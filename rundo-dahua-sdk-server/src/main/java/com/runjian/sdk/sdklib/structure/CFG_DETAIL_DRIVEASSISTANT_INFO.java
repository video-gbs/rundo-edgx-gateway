package com.runjian.sdk.sdklib.structure;

import com.runjian.sdk.sdklib.NetSDKLib;

/**
 * @author 47081
 * @version 1.0
 * @description 驾驶辅助场景配置
 * @date 2021/2/22
 */
public class CFG_DETAIL_DRIVEASSISTANT_INFO extends NetSDKLib.SdkStructure {
  /** 是否有效 */
  public boolean bValid;
  /** 车宽 0-5000mm 单位mm */
  public int nVehicleWidth;
  /** 相机高度 0-5000mm 单位mm */
  public int nCamHeight;
  /** 车头到相机的距离 0-5000mm 单位mm */
  public int nCamToCarHead;
  /** 保留 */
  public byte[] byReserved = new byte[56];
}
