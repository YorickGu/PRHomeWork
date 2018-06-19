package edu.ict;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

public class Utils {

	public static List<SamplePoint> readSamplePoints(String file) throws Exception {
		List<SamplePoint> points = new ArrayList<SamplePoint>();
		// 读取数据文件，初始化样本点
		System.out.println("读取数据文件...");
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = "";
		int linecount = 0;
		while ((line = in.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f,");
			float[] values = new float[st.countTokens()];
			for (int i = 0; i < values.length; i++) {
				values[i] = Float.parseFloat(st.nextToken());
			}
			SamplePoint point = new SamplePoint((++linecount), values);
			points.add(point);

			System.out.println(point);
		}
		in.close();
		return points;
	}

	/**
	 * 读取已标注样本，并且样本采用增广向量表示
	 * 
	 * @param file
	 * @return
	 * @throws Exception
	 */
	public static List<SamplePoint> readLabeledSamplePoints(String file) throws Exception {
		List<SamplePoint> points = new ArrayList<SamplePoint>();
		// 读取数据文件，初始化样本点
		System.out.println("读取数据文件...");
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line = "";
		int linecount = 0;
		while ((line = in.readLine()) != null) {
			StringTokenizer st = new StringTokenizer(line, " \t\n\r\f,");
			float[] values = new float[st.countTokens()];
			SamplePoint point = new SamplePoint((++linecount), values);
			for (int i = 0; i < values.length; i++) {
				if (i == values.length - 1) {
					// 最后一个数为类别号
					values[i] = 1;
					int groupNumber = Integer.parseInt(st.nextToken());
					point.setGroupNumber(groupNumber);
				} else {
					values[i] = Float.parseFloat(st.nextToken());
				}
			}
			points.add(point);

			System.out.println(point);
		}
		in.close();
		return points;
	}

	public static <K, O> List<O> hashObject(K key, O obj, Map<K, List<O>> mapObjs) {
		List<O> objList = mapObjs.get(key);
		if (objList == null) {
			objList = new ArrayList<O>();
			mapObjs.put(key, objList);
		}
		objList.add(obj);
		return objList;
	}
}
