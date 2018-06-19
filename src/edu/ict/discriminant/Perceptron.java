package edu.ict.discriminant;

import java.util.List;

import org.apache.commons.lang.ArrayUtils;

import edu.ict.SamplePoint;
import edu.ict.Utils;

/**
 * 感知器算法实现类
 * 
 * @author ysh
 * 
 */
public class Perceptron {
	/**
	 * 保存所有样本点
	 */
	private List<SamplePoint> points;

	/**
	 * 权向量
	 */
	private float[] w;

	/**
	 * 增量调整系数
	 */
	private float coef;

	/**
	 * 感知器算法实现类构造函数
	 * 
	 * @param points
	 *            样本点
	 * @param w
	 *            初始权向量
	 * @param coef
	 *            增量调整系数
	 */
	public Perceptron(List<SamplePoint> points, float[] w, float coef) {
		super();
		this.points = points;
		this.w = w;
		this.coef = coef;

		for (SamplePoint point : points) {
			if (point.getGroupNumber() != 1) {
				// 不属于第一类的样本模式向量都乘与-1
				float[] values = point.getValues();
				for (int i = 0; i < values.length; i++) {
					values[i] = -values[i];
				}
			}
		}
	}

	/**
	 * 运行感知器算法
	 */
	public void run() {

		boolean ok = false;

		// 迭代次数与权向量（w）的修正次数
		int nIters = 1, count = 1;

		while (!ok) {
			ok = true;

			System.out.println("--------迭代次序号：" + nIters + "-------");

			for (SamplePoint point : points) {
				if (!discriminant(point)) {
					// point被错分
					ok = false;
					float[] values = point.getValues();

					System.out.println(point + " 被错分");
					StringBuffer sbuf = new StringBuffer("修正权向量 w(" + count + ")="
							+ ArrayUtils.toString(w) + " -> ");

					for (int i = 0; i < w.length; i++) {
						w[i] += coef * values[i];
					}
					count++;

					sbuf.append("w(" + count + ")=" + ArrayUtils.toString(w));
					System.out.println(sbuf.toString());
				}
			}
			nIters++;
		}

		System.out.println("\n最终w为：" + ArrayUtils.toString(w));
	}

	private boolean discriminant(SamplePoint point) {
		float[] values = point.getValues();
		float discrnt_value = 0;
		for (int i = 0; i < values.length; i++) {
			discrnt_value += w[i] * values[i];
		}
		return discrnt_value > 0;
	}

	public static void main(String[] args) throws Exception {
		String file = "perceptron_data_1.txt";
		List<SamplePoint> points = Utils.readLabeledSamplePoints(file);
		float[] w = new float[] { 0, 0, 0, 0 };
		Perceptron perceptron = new Perceptron(points, w, 1.0f);
		perceptron.run();
	}
}
