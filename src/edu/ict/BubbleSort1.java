package edu.ict;
import java.util.Arrays;
/**
 * 鍐掓场鎺掑簭鏀硅繘鐗�
 * @author mmz
 *
 */
public class BubbleSort1 {
    public static void BubbleSort(int[] arr) {
        boolean flag = true;
        while(flag){
            int temp;//瀹氫箟涓�涓复鏃跺彉閲�
            for(int i=0;i<arr.length-1;i++){//鍐掓场瓒熸暟锛宯-1瓒�
                for(int j=0;j<arr.length-i-1;j++){
                    if(arr[j+1]<arr[j]){
                        temp = arr[j];
                        arr[j] = arr[j+1];
                        arr[j+1] = temp;
                        flag = true;
                    }
                }
                if(!flag){
                    break;//鑻ユ灉娌℃湁鍙戠敓浜ゆ崲锛屽垯閫�鍑哄惊鐜�
                }
            }
        }
    }
    public static void main(String[] args) {
        int arr[] = new int[]{1,6,2,2,5};
        BubbleSort1.BubbleSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}