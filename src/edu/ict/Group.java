package edu.ict;

/**
 * 聚类
 * 
 * @author gyj
 * 
 */
public class Group {

	/**
	 * 聚类代号（数字）
	 */
	private int number;

	/**
	 * 均值点
	 */
	private SamplePoint meanPoint;

	private float meanDistance;

	/**
	 * 根据聚类代号构造聚类
	 * 
	 * @param clusterNumber
	 */
	public Group(int number) {
		this.number = number;
	}

	/**
	 * 设置均值点
	 * 
	 * @param meanPoint
	 */
	public void setMeanPoint(SamplePoint meanPoint) {
		this.meanPoint = meanPoint;
	}

	/**
	 * 取得均值点
	 * 
	 * @return
	 */
	public SamplePoint getMeanPoint() {

		return this.meanPoint;

	}

	/**
	 * 取得聚类代号
	 * 
	 * @return
	 */
	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public float getMeanDistance() {
		return meanDistance;
	}

	public void setMeanDistance(float meanDistance) {
		this.meanDistance = meanDistance;
	}

	@Override
	public String toString() {
		StringBuffer sbuf = new StringBuffer("groups[" + (number + 1) + "],均值点");
		sbuf.append("(");
		float[] values = meanPoint.getValues();
		for (int i = 0; i < values.length; i++) {
			sbuf.append(values[i]);
			if (i != values.length - 1)
				sbuf.append(", ");
		}
		sbuf.append(")");
		return sbuf.toString();
	}

}
