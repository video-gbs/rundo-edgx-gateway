package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib.SdkStructure;
/**
 * @description 任务全局配置
 * @author 119178
 * @date 2021/3/16
 */
public class NET_ANALYSE_TASK_GLOBAL extends SdkStructure{
	/**
	 * 车道信息数量
	 */
	public int										nLanesNum;                      
	/**
	 * 车道信息 每个车道两条边界线
	 */
	public	NET_ANALYSE_TASK_GLOBAL_LANES[]			stuLanes=(NET_ANALYSE_TASK_GLOBAL_LANES[])new NET_ANALYSE_TASK_GLOBAL_LANES().toArray(8);					
	/**
	 * 标定区域个数
	 */
	public int										nCalibrateArea;					
	/**
	 * 标定区域
	 */
	public	NET_ANALYSE_TASK_GLOBAL_CALIBRATEAREA[]	stuCalibrateArea = (NET_ANALYSE_TASK_GLOBAL_CALIBRATEAREA[])new NET_ANALYSE_TASK_GLOBAL_CALIBRATEAREA().toArray(32);
	/**
	 * 保留字节
	 */
	public	byte[]									byReserved=new byte[1024];				
}
