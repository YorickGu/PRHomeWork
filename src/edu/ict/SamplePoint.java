package edu.ict;

/**
 * 样本点类
 * 
 * @author ysh
 * 
 */
public class SamplePoint {

	private int index;

	/**
	 * 模式样本的特征向量
	 */
	private float[] values;

	/**
	 * 所属类别的代号
	 */
	private int groupNumber;

	public SamplePoint(int index, float[] values) {
		this.index = index;
		this.values = values;
	}

	public int getGroupNumber() {
		return groupNumber;
	}

	public void setGroupNumber(int groupNumber) {
		this.groupNumber = groupNumber;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public float[] getValues() {
		return values;
	}

	public void setValues(float[] values) {
		this.values = values;
	}

	public int getDimension() {
		return values.length;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer();
		if (index > 0)
			sbuf.append("x" + index + " (");
		else
			sbuf.append("均值点 (");
		for (int i = 0; i < values.length; i++) {
			sbuf.append(values[i]);
			if (i != values.length - 1)
				sbuf.append(", ");
		}
		sbuf.append(")");
		return sbuf.toString();
	}

	/**
	 * 求两模式样本间的欧式距离
	 * 
	 * @param dp1
	 *            模式样本1
	 * @param dp2
	 *            模式样本2
	 * @return 两模式样本间的欧式距离
	 */
	public static float distance(SamplePoint dp1, SamplePoint dp2) {
		float result = 0;
		float[] values1 = dp1.getValues();
		float[] values2 = dp2.getValues();
		for (int i = 0; i < values1.length; i++) {
			float dis = values1[i] - values2[i];
			result += dis * dis;
		}
		return (float) Math.sqrt(result);
	}

}
