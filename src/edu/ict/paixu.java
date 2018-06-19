package 冒泡排序;
import java.util.Arrays;
/**
 * 冒泡排序改进版
 * @author mmz
 *
 */
public class BubbleSort1 {
    public static void BubbleSort(int[] arr) {
        boolean flag = true;
        while(flag){
            int temp;//定义一个临时变量
            for(int i=0;i<arr.length-1;i++){//冒泡趟数，n-1趟
                for(int j=0;j<arr.length-i-1;j++){
                    if(arr[j+1]<arr[j]){
                        temp = arr[j];
                        arr[j] = arr[j+1];
                        arr[j+1] = temp;
                        flag = true;
                    }
                }
                if(!flag){
                    break;//若果没有发生交换，则退出循环
                }
            }
        }
    }
    public static void main(String[] args) {
        int arr[] = new int[]{1,6,2,2,5};
        BubbleSort.BubbleSort(arr);
        System.out.println(Arrays.toString(arr));
    }
}