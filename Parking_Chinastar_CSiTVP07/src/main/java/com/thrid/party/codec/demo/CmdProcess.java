package com.thrid.party.codec.demo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * @author SZP 对下行数据编码
 *
 */
public class CmdProcess {

	private String identifier = "0";
	private Integer intIdentifier = 0;
	private String msgType = "deviceReq";
	private int hasMore = 0;
	private int mid = 0;
	private int reportmid;
	private int errcode = 0;

	// 云端回应
	private static final byte B_CMD_RESPONSE = (byte) 0xAA;
	private JsonNode paras;

	private String cmd = "OUTSET";

	/**
	 * 配置命令报文
	 */
	private static final byte B_CMD_SET = 0x04;
	/**
	 * 读取开机信息报文
	 */
	private static final byte B_TO_REREAD_INFO = 0x06;
	/**
	 * 布防撤防命令报文
	 */
	private static final byte B_TO_SET_WORK_PATTERN = 0x05;
	/**
	 * 采集背景磁场
	 */
	private static final byte B_TO_COLLECT_MAGNETIC = 0x0B;
	/**
	 * 撤销报警命令报文
	 */
	private static final byte B_TO_CANCEL_WARN = 0x06;
	/**
	 * 恢复出厂设置
	 */
	private static final byte B_TO_FACTORY_RESET = 0x07;
	/**
	 * 设置休眠模式
	 */
	private static final byte B_TO_DORMANCY = 0x08;
	/**
	 * 终端校准
	 */
	private static final byte B_TO_CALIBRAT = 0x09;
	/**
	 * 复位命令报文
	 */
	private static final byte B_RESET = 0x05;

	/**
	 * 功能码
	 */
	private byte bFunctionCode;

	/**
	 * 被应答功能码
	 */
	private byte returnFunctionCode;
	/**
	 * 报文长度
	 */
	private byte dataLength;

	/**
	 * 将要配置的新ID
	 */
	private int iNewId;

	/**
	 * 工作模式
	 */
	private int patternType;
	/**
	 * 协议版本号
	 */
	private int PV;

	/**
	 * 终端ID
	 */
	private String ID;

	/**
	 * 消息序号
	 */
	private int serial;
	/**
	 * 终端定时上报间隔
	 */
	private int timeInterval;
	/**
	 * 采样周期
	 */
	private int TimelySampleInterval;

	private long IP;

	private int port;

	private long RtcTime;

	/**
	 * 检测门限等级
	 */
	private int DetectThresholdLevel;
	/**
	 * 起始码
	 */
	private byte protocolVersion = (byte) 0x02;

	// 用于存储平台自动应答时返回的request字段，该字段即设备的上行数据
	byte[] deviceRequest;

	byte[] deviceRequestByte;
	/**
	 * 无车磁场变化量
	 */
	private int VarOfMagneticFieldWithoutCar;
	/**
	 * 有车磁场变化量
	 */
	private int VarOfMagneticFieldWithCar;
	/**
	 * 雷达灵敏度
	 */
	private int RadarSensitivity;

	public CmdProcess() {
	}

	public CmdProcess(ObjectNode input) {

		try {

			try {
				this.identifier = input.get("identifier").asText();
				// System.out.println("identifier"+this.identifier);
			} catch (Exception e) {
				this.identifier = "-1";
			}

			this.paras = input.get("paras");

			this.msgType = input.get("msgType").asText();

			if (msgType.equals("cloudRsp")) {

				this.errcode = input.get("errcode").asInt();
				deviceRequest = input.get("request").binaryValue();
				bFunctionCode = B_CMD_RESPONSE;
				dataLength = 0x01;

			} else {
				this.mid = input.get("mid").asInt();// 未使用mid 在此处不能放出来，否则会造成程序异常
				this.cmd = input.get("cmd").asText();
				switch (this.cmd) {
				case "Configuration":// 设置
					bFunctionCode = B_CMD_SET;
					dataLength = 0x10;

					this.paras = input.get("paras");

					iNewId = this.paras.get("NewID").asInt();
					timeInterval = this.paras.get("TimelyReportInterval").asInt();
					TimelySampleInterval = this.paras.get("TimelySampleInterval").asInt();

					DetectThresholdLevel = this.paras.get("DetectThresholdLevel").asInt();
					VarOfMagneticFieldWithoutCar = this.paras.get("VarOfMagneticFieldWithoutCar").asInt();
					VarOfMagneticFieldWithCar = this.paras.get("VarOfMagneticFieldWithCar").asInt();
					RadarSensitivity = this.paras.get("RadarSensitivity").asInt();

					break;
				case "Guard_OR_Disguard":// 布防撤防命令
					bFunctionCode = B_TO_SET_WORK_PATTERN;
					dataLength = 0x02;

					this.paras = input.get("paras");

					patternType = this.paras.get("Data").asInt();

					break;
				case "Reset":// 复位
					bFunctionCode = B_RESET;
					dataLength = 0x00;
					break;
				case "EnterSleepMode":// 休眠
					bFunctionCode = B_TO_DORMANCY;
					dataLength = 0x00;
					break;
				case "SampleBGMagneticField":// 采集背景磁场
					bFunctionCode = B_TO_COLLECT_MAGNETIC;
					dataLength = 0x00;
					break;
				case "ReadBootMessage":// 重发设备信息
					bFunctionCode = B_TO_REREAD_INFO;
					dataLength = 0x00;
					break;
				case "RestoreFactoryDefault":// 恢复出厂设置
					bFunctionCode = B_TO_FACTORY_RESET;
					dataLength = 0x00;
					break;
				case "Calibration": // 终端校准
					bFunctionCode = B_TO_CALIBRAT;
					dataLength = 0x00;
					break;
				case "UpgradeMode": // 升级模式切换
					bFunctionCode = 0x0D;
					dataLength = 0x00;
					break;
				case "RtcTime": // RTC校准
					bFunctionCode = 0x0A;
					dataLength = 0x04;
					DateFormat df = new SimpleDateFormat("yyyy-MM-DD HH:mm:ss");
					RtcTime = df.parse(df.format(new Date())).getTime();

					break;
				case "SetIP": // 设置IP
					bFunctionCode = B_TO_COLLECT_MAGNETIC;
					dataLength = 0x04;
					this.paras = input.get("paras");

					String str = this.paras.get("IPAddress").asText();
					if (str.length() > 8) {
						String s[] = str.split("\\.");
						String ss = String.format("%02x", Integer.valueOf(s[3], 10)) + ""
								+ String.format("%02x", Integer.valueOf(s[2], 10)) + ""
								+ String.format("%02x", Integer.valueOf(s[1], 10)) + ""
								+ String.format("%02x", Integer.valueOf(s[0], 10));

						IP = Long.parseLong(ss, 16);
					} else {
						IP = Long.parseLong(str, 10);
					}
					port = this.paras.get("Port").asInt();
					break;
				case "SetOTAIP":
					bFunctionCode = 0x0C;
					dataLength = 0x08;
					this.paras = input.get("paras");

					String str2 = this.paras.get("IPAddress").asText();
					if (str2.length() > 8) {
						String s[] = str2.split("\\.");
						String ss = String.format("%02x", Integer.valueOf(s[3], 10)) + ""
								+ String.format("%02x", Integer.valueOf(s[2], 10)) + ""
								+ String.format("%02x", Integer.valueOf(s[1], 10)) + ""
								+ String.format("%02x", Integer.valueOf(s[0], 10));

						IP = Long.parseLong(ss, 16);
					} else {
						IP = Long.parseLong(str2, 10);
					}
					port = this.paras.get("Port").asInt();
					break;
				default:
					break;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public byte[] toByte() {
		try {
			if (this.msgType.equals("cloudReq")) {

				byte[] bytesRead = null;
				int idid = 0;
				try {
					idid = Integer.parseInt(this.identifier);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
				switch (this.cmd) {
				case "Configuration": {// 设置地址
					bytesRead = new byte[26];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];
					byte[] bytesNewId = Utilty.getInstance().int2Bytes(this.iNewId, 2);
					bytesRead[8] = bytesNewId[1];
					bytesRead[9] = bytesNewId[0];
					byte[] bytesInterval = Utilty.getInstance().int2Bytes(this.timeInterval, 2);
					bytesRead[10] = bytesInterval[1];
					bytesRead[11] = bytesInterval[0];
					bytesRead[12] = 0x00;
					bytesRead[13] = 0x00;

					byte[] bytesSampleInterval = Utilty.getInstance().int2Bytes(this.TimelySampleInterval, 2);
					bytesRead[14] = bytesSampleInterval[1];
					bytesRead[15] = bytesSampleInterval[0];

					bytesRead[16] = (byte) this.DetectThresholdLevel;
					bytesRead[17] = (byte) this.VarOfMagneticFieldWithoutCar;
					bytesRead[18] = (byte) this.VarOfMagneticFieldWithCar;
					bytesRead[19] = (byte) this.RadarSensitivity;
					bytesRead[20] = 0x00;
					bytesRead[21] = 0x00;
					bytesRead[22] = 0x00;
					bytesRead[23] = 0x00;
					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);
					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
					bytesRead[24] = bytesCRC[1];
					bytesRead[25] = bytesCRC[0];
					break;
				}
				case "Guard_OR_Disguard": {// 布防撤防命令

					bytesRead = new byte[12];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					byte[] bytesPattern = Utilty.getInstance().int2Bytes(patternType, 2);
					bytesRead[8] = bytesPattern[1];
					bytesRead[9] = bytesPattern[0];
					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);

					bytesRead[10] = bytesCRC[1];
					bytesRead[11] = bytesCRC[0];
					break;
				}
				case "Reset": {// 复位

					bytesRead = new byte[10];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);

					bytesRead[8] = bytesCRC[1];
					bytesRead[9] = bytesCRC[0];
					break;

				}
				case "ReadBootMessage": {// 重发设备信息

					bytesRead = new byte[10];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);

					bytesRead[8] = bytesCRC[1];
					bytesRead[9] = bytesCRC[0];
					break;
				}
				case "RestoreFactoryDefault": {// 恢复出厂设置

					bytesRead = new byte[10];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
					bytesRead[8] = bytesCRC[1];
					bytesRead[9] = bytesCRC[0];
					break;
				}
				case "EnterSleepMode": {// 休眠模式

					bytesRead = new byte[10];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
					bytesRead[8] = bytesCRC[1];
					bytesRead[9] = bytesCRC[0];
					break;
				}
				case "SampleBGMagneticField": {// 采集背景磁场

					bytesRead = new byte[10];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);

					bytesRead[8] = bytesCRC[1];
					bytesRead[9] = bytesCRC[0];
					break;
				}
				case "Calibration": { // 终端校准
					bytesRead = new byte[10];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
					bytesRead[8] = bytesCRC[1];
					bytesRead[9] = bytesCRC[0];
					break;
				}
				case "RtcTime": { // RTC校准
					bytesRead = new byte[14];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];
					byte[] bytesRtctime = Utilty.getInstance().long2bytes(this.RtcTime);
					bytesRead[8] = bytesRtctime[0];
					bytesRead[9] = bytesRtctime[1];
					bytesRead[10] = bytesRtctime[2];
					bytesRead[11] = bytesRtctime[3];

					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
					bytesRead[12] = bytesCRC[1];
					bytesRead[13] = bytesCRC[0];
					break;
				}
				case "SetIP":{  // 设置IP
					bytesRead=  new byte[16];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid =  Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength =  Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					byte[] bytesIP =  Utilty.getInstance().long2bytes(this.IP);
					bytesRead[8] = bytesIP[0];
					bytesRead[9] = bytesIP[1];
					bytesRead[10] = bytesIP[2];
					bytesRead[11] = bytesIP[3];
					byte[] bytesPort =  Utilty.getInstance().int2Bytes(this.port,2);
					bytesRead[12] = bytesPort[1];
					bytesRead[13] = bytesPort[0];
					bytesRead[14]=0x00;
					bytesRead[15]=0x00;
					//CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);
					
					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
					bytesRead[16] = bytesCRC[1];
					bytesRead[17] = bytesCRC[0];
					break;
				}
				case "SetOTAIP":{  // 设置IP
					bytesRead=  new byte[16];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid =  Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength =  Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					byte[] bytesIP =  Utilty.getInstance().long2bytes(this.IP);
					bytesRead[8] = bytesIP[0];
					bytesRead[9] = bytesIP[1];
					bytesRead[10] = bytesIP[2];
					bytesRead[11] = bytesIP[3];
					byte[] bytesPort =  Utilty.getInstance().int2Bytes(this.port,2);
					bytesRead[12] = bytesPort[1];
					bytesRead[13] = bytesPort[0];
					bytesRead[14]=0x00;
					bytesRead[15]=0x00;
					//CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);
					
					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
					bytesRead[16] = bytesCRC[1];
					bytesRead[17] = bytesCRC[0];
					break;
				}
				case "UpgradeMode":{
					bytesRead = new byte[10];
					bytesRead[0] = protocolVersion;
					bytesRead[1] = bFunctionCode;
					bytesRead[2] = (byte) (idid & 0xFF);
					bytesRead[3] = (byte) (idid >> 8);
					byte[] bytesmid = Utilty.getInstance().int2Bytes(this.mid, 2);
					bytesRead[4] = bytesmid[1];
					bytesRead[5] = bytesmid[0];
					byte[] bytesDataLength = Utilty.getInstance().int2Bytes(this.dataLength, 2);
					bytesRead[6] = bytesDataLength[1];
					bytesRead[7] = bytesDataLength[0];

					// CRC校验
					byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);

					byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
					bytesRead[8] = bytesCRC[1];
					bytesRead[9] = bytesCRC[0];
				}
				default:
					break;
				}
				return bytesRead;
			} else if (this.msgType.equals("cloudRsp")) {

				byte[] bytesRead = new byte[12];
				/*
				 * 平台对上行数据的自动应答过程中，并不会每次都去调用带参的构造方法（猜测），所以数据的具体组装需要在toByte方法中进行以保证回复及下发的稳定性
				 */
				bFunctionCode = B_CMD_RESPONSE;
				dataLength = 2;
				/*
				 * 在此对捕获的设备命令进行校验，如果无错误，将errcode置为0，若校验错误，将其置为2
				 */
				boolean isValide = false;
				if (deviceRequest != null) {
					isValide = Utilty.getInstance().isValid(deviceRequest);
				}

				bytesRead = new byte[12];
				bytesRead[0] = (byte) protocolVersion;
				bytesRead[1] = B_CMD_RESPONSE;
				// byte[] bytesId = Utilty.getInstance().int2Bytes(this.intIdentifier, 2);;
				bytesRead[2] = deviceRequest[2];
				bytesRead[3] = deviceRequest[3];
				// byte[] bytesmid = Utilty.getInstance().int2Bytes(this.reportmid, 2);
				bytesRead[4] = deviceRequest[4];
				bytesRead[5] = deviceRequest[5];
				bytesRead[6] = dataLength;
				bytesRead[7] = 0;
				bytesRead[8] = 0;// (byte) (isValide?0:2);
				bytesRead[9] = deviceRequest[1];
				byte[] bytesNoCRC = Arrays.copyOf(bytesRead, bytesRead.length - 2);
				byte[] bytesCRC = Utilty.getInstance().CRC16(bytesNoCRC);
				bytesRead[10] = bytesCRC[1];
				bytesRead[11] = bytesCRC[0];
				return bytesRead;

			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
