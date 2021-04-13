package com.ai.aiIdentification.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import com.amazonaws.util.json.JSONException;
import com.amazonaws.util.json.JSONObject;

public class GdalImage {

	static {
		gdal.AllRegister();
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8","YES"); 
	}
	private final String tiffPath;//路径
    private final Dataset rds;//影像

    private final double[] geoTransform;//获取tiff左上角坐标、每个像素宽度等
    private final int x;//tiff宽(像素)
    private final int y;//tiff高(像素)
    private final int b;//波段数
    private final String proj;//tiff坐标系信息
    private final double[] ulCoord = new double[2];//左上角坐标  
    private final double[] brCoord = new double[2];//右下角坐标
    private final int dataType;//影像数据类型
    
    public GdalImage(String tiffPath) {
    	this.tiffPath=tiffPath;
        //读取影像
        rds = gdal.Open(tiffPath, gdalconst.GA_ReadOnly);
        if (rds == null) {
            throw new RuntimeException("读取tiff文件异常:\n" + tiffPath + "\n" + gdal.GetLastErrorMsg());
        }
        //宽、高、波段数
        x = rds.getRasterXSize();
        y = rds.getRasterYSize();
        b = rds.getRasterCount();
      //从波段中获取影像的数据类型，gdal中波段索引从1开始
        dataType = rds.GetRasterBand(1).GetRasterDataType();
        //六参数信息
        geoTransform = rds.GetGeoTransform();
        //影像左上角投影坐标
        ulCoord[0] = geoTransform[0];
        ulCoord[1] = geoTransform[3];
        //影像右下角投影坐标
        brCoord[0] = geoTransform[0] + x * geoTransform[1] + y * geoTransform[2];
        brCoord[1] = geoTransform[3] + x * geoTransform[4] + y * geoTransform[5];

        //影像投影信息
        proj = rds.GetProjection();
    }
    /**
     * @param latMin 左上角经度
     * @param latMax 右下角经度
     * @param lonMin 右下角纬度
     * @param lonMax 左上角纬度
     * @param workPath 
     * */
    public String clipTiff(double latMin,double latMax,double lonMin, double lonMax, String workPath) {
    	double dx = geoTransform[1], dy = geoTransform[5];
    	//设置要裁剪的起始像元位置，以及各方向的像元数
        int startX = (int) ((latMin - ulCoord[0]) / dx);
        int startY = (int) ((lonMax - ulCoord[1]) / dy);
        int clipX = (int) ((latMax - latMin) / dx + 0.5);
        int clipY = (int) ((lonMin - lonMax) / dy + 0.5);
        if (startX > x || startY > y || startX+clipX < 0 || startY+clipY < 0) {
            return null;//不在范围内，直接返回null
        }
        if (clipX > x || clipY > y) {
            return null;//切片比tiff还大，出于效率考虑就不处理了
        }
      //计算裁剪后的左上角坐标
        double[] destGeoTransform=geoTransform;
        destGeoTransform[0] = geoTransform[0] + startX * geoTransform[1] + startY * geoTransform[2];
        destGeoTransform[3] = geoTransform[3] + startX * geoTransform[4] + startY * geoTransform[5];
      //创建结果图像
        Driver driver = gdal.GetDriverByName("GTIFF");
        Dataset outputDs = driver.Create(workPath+"/clip1.tif", clipX, clipY, b, dataType);
        outputDs.SetGeoTransform(destGeoTransform);
        outputDs.SetProjection(proj);
      //按band读取
        for(int i = 0; i < clipY; i++){
          //按行读取
          for(int j = 1; j <= b; j++){
            Band orgBand = rds.GetRasterBand(j);
            int[] cache = new int[clipX];
            //从位置x开始，只读一行
            orgBand.ReadRaster(startX, startY + i, clipX, 1, cache);
            Band desBand = outputDs.GetRasterBand(j);
            //从左上角开始，只写一行
            desBand.WriteRaster(0, i, clipX, 1, cache);
            desBand.FlushCache();
          }
        }

        //释放资源
//        rds.delete();
        outputDs.delete();
        return workPath+"/clip1.tif";
    }
    /**
     * tif转png
     * */
    public String tif2png() {
    	Driver hDriver= gdal.GetDriverByName("PNG");
    	File temp =new File(tiffPath);
    	
    	String tempPng=temp.getParent()+"/input.png";
    	Dataset png=hDriver.CreateCopy(tempPng, rds);
//        rds.delete();
        png.delete();
        hDriver.delete();
		return tempPng;
    	
    }
    /**
     * 创建geojson数据
     * @throws JSONException 
     * */
    public String createJson(String txt) {
    	File txtFile= new File(txt);
    	ArrayList list = new ArrayList();
		try {
			FileInputStream fileInputStream = new FileInputStream(txtFile);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			String text = null;
			while ((text = bufferedReader.readLine()) != null) {
					list.add(text.trim());
			}
			fileInputStream.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String geo = null;
		try {
			geo = getPolygonGeoJson(list);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		append2File(txtFile.getParent()+"/"+txtFile.getName().replaceAll(".txt", ".json"), geo);
		rds.delete();
		return geo;
    	
    }
	public String getPolygonGeoJson(ArrayList list) throws JSONException {
		String head = "{\"type\": \"FeatureCollection\"," + "\"features\": [";
        String end = "  ] }";
        String geometry = " { \"type\":\"Feature\",\"geometry\":";
        String properties = ",\"properties\":{ \"name\":";
        String geo = null;
        double dx = geoTransform[1], dy = geoTransform[5];
		for(int i=0;i<list.size();i++) {
			JSONObject js = new JSONObject();
			List<Object> ptsTotal = new ArrayList<Object>();
			List<Object> pts = new ArrayList<Object>();
			String[] tempText=list.get(i).toString().split("\\s+");
            List<Double> pt1 = new ArrayList<Double>();
            pt1.add(ulCoord[0]+Double.parseDouble(tempText[0])*dx);
            pt1.add(ulCoord[0]+Double.parseDouble(tempText[1])*dy);
            List<Double> pt2 = new ArrayList<Double>();
            pt2.add(ulCoord[0]+Double.parseDouble(tempText[2])*dx);
            pt2.add(ulCoord[0]+Double.parseDouble(tempText[3])*dy);
            List<Double> pt3 = new ArrayList<Double>();
            pt3.add(ulCoord[0]+Double.parseDouble(tempText[4])*dx);
            pt3.add(ulCoord[0]+Double.parseDouble(tempText[5])*dy);
            List<Double> pt4 = new ArrayList<Double>();
            pt4.add(ulCoord[0]+Double.parseDouble(tempText[6])*dx);
            pt4.add(ulCoord[0]+Double.parseDouble(tempText[7])*dy);
            
            pts.add(pt1);
            pts.add(pt4);
            pts.add(pt3);
            pts.add(pt2);
            pts.add(pt1);
            
            ptsTotal.add(pts);
            
            js.put("type", "Polygon");
            js.put("coordinates", ptsTotal);
            geo = geometry + js.toString() + properties + tempText[8]+ "} }" + "," + geo;
		}
		if (geo.contains(",")) {
            geo = geo.substring(0, geo.lastIndexOf(","));
        }

        geo = head + geo + end;
		return geo;
	}
	public void append2File(String file, String content) {
        FileWriter fw = null;

        try {
        	//如果文件存在，则追加内容；如果文件不存在，则创建文件
            File f = new File(file);
            fw = new FileWriter(f, true);
        } 
        catch (IOException e) {
            e.printStackTrace();
        }

        PrintWriter pw = new PrintWriter(fw);
        pw.println(content);
        pw.flush();

        try {
            fw.flush();
            pw.close();
            fw.close();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
    }
}
