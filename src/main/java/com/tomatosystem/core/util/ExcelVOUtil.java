package com.tomatosystem.core.util;

import com.tomatosystem.core.vo.ExcelVO;

public class ExcelVOUtil {
	public static String getCellValue(ExcelVO vo, int index){
		if(index == 0) return vo.getCELL0();
		else if(index == 1) return vo.getCELL1();
		else if(index == 2) return vo.getCELL2();
		else if(index == 3) return vo.getCELL3();
		else if(index == 4) return vo.getCELL4();
		else if(index == 5) return vo.getCELL5();
		else if(index == 6) return vo.getCELL6();
		else if(index == 7) return vo.getCELL7();
		else if(index == 8) return vo.getCELL8();
		else if(index == 9) return vo.getCELL9();
		else if(index == 10) return vo.getCELL10();
		else if(index == 11) return vo.getCELL11();
		else if(index == 12) return vo.getCELL12();
		else if(index == 13) return vo.getCELL13();
		else if(index == 14) return vo.getCELL14();
		else if(index == 15) return vo.getCELL15();
		else if(index == 16) return vo.getCELL16();
		else if(index == 17) return vo.getCELL17();
		else if(index == 18) return vo.getCELL18();
		else if(index == 19) return vo.getCELL19();
		else if(index == 20) return vo.getCELL20();
		else if(index == 21) return vo.getCELL21();
		else if(index == 22) return vo.getCELL22();
		else if(index == 23) return vo.getCELL23();
		else if(index == 24) return vo.getCELL24();
		else if(index == 25) return vo.getCELL25();
		else if(index == 26) return vo.getCELL26();
		else if(index == 27) return vo.getCELL27();
		else if(index == 28) return vo.getCELL28();
		else if(index == 29) return vo.getCELL29();
		else if(index == 30) return vo.getCELL30();
		else if(index == 31) return vo.getCELL31();
		else if(index == 32) return vo.getCELL32();
		else if(index == 33) return vo.getCELL33();
		else if(index == 34) return vo.getCELL34();
		else if(index == 35) return vo.getCELL35();
		else if(index == 36) return vo.getCELL36();
		else if(index == 37) return vo.getCELL37();
		else if(index == 38) return vo.getCELL38();
		else if(index == 39) return vo.getCELL39();
		else if(index == 40) return vo.getCELL40();
		else if(index == 41) return vo.getCELL41();
		else if(index == 42) return vo.getCELL42();
		else if(index == 43) return vo.getCELL43();
		else if(index == 44) return vo.getCELL44();
		else if(index == 45) return vo.getCELL45();
		else if(index == 46) return vo.getCELL46();
		else if(index == 47) return vo.getCELL47();
		else if(index == 48) return vo.getCELL48();
		else if(index == 49) return vo.getCELL49();
		else if(index == 50) return vo.getCELL50();
		else if(index == 51) return vo.getCELL51();
		else if(index == 52) return vo.getCELL52();
		else if(index == 53) return vo.getCELL53();
		else if(index == 54) return vo.getCELL54();
		else if(index == 55) return vo.getCELL55();
		else if(index == 56) return vo.getCELL56();
		else if(index == 57) return vo.getCELL57();
		else if(index == 58) return vo.getCELL58();
		else if(index == 59) return vo.getCELL59();
		else if(index == 60) return vo.getCELL60();
		
		return "";
	}
	
	public static String getCellValue(ExcelVO vo, String index){
		if(index == "ERR"){
			return vo.getERRMSG();
		}else{
			return getCellValue(vo, Integer.parseInt(index));
		}
	}
	
	public static void setCellValue(ExcelVO item, int cellIdx, String value){
		if(cellIdx == 0) item.setCELL0(value);
		else if(cellIdx == 1) item.setCELL1(value);
		else if(cellIdx == 2) item.setCELL2(value);
		else if(cellIdx == 3) item.setCELL3(value);
		else if(cellIdx == 4) item.setCELL4(value);
		else if(cellIdx == 5) item.setCELL5(value);
		else if(cellIdx == 6) item.setCELL6(value);
		else if(cellIdx == 7) item.setCELL7(value);
		else if(cellIdx == 8) item.setCELL8(value);
		else if(cellIdx == 9) item.setCELL9(value);
		else if(cellIdx == 10) item.setCELL10(value);
		else if(cellIdx == 11) item.setCELL11(value);
		else if(cellIdx == 12) item.setCELL12(value);
		else if(cellIdx == 13) item.setCELL13(value);
		else if(cellIdx == 14) item.setCELL14(value);
		else if(cellIdx == 15) item.setCELL15(value);
		else if(cellIdx == 16) item.setCELL16(value);
		else if(cellIdx == 17) item.setCELL17(value);
		else if(cellIdx == 18) item.setCELL18(value);
		else if(cellIdx == 19) item.setCELL19(value);
		else if(cellIdx == 20) item.setCELL20(value);
		else if(cellIdx == 21) item.setCELL21(value);
		else if(cellIdx == 22) item.setCELL22(value);
		else if(cellIdx == 23) item.setCELL23(value);
		else if(cellIdx == 24) item.setCELL24(value);
		else if(cellIdx == 25) item.setCELL25(value);
		else if(cellIdx == 26) item.setCELL26(value);
		else if(cellIdx == 27) item.setCELL27(value);
		else if(cellIdx == 28) item.setCELL28(value);
		else if(cellIdx == 29) item.setCELL29(value);
		else if(cellIdx == 30) item.setCELL30(value);
		else if(cellIdx == 31) item.setCELL31(value);
		else if(cellIdx == 32) item.setCELL32(value);
		else if(cellIdx == 33) item.setCELL33(value);
		else if(cellIdx == 34) item.setCELL34(value);
		else if(cellIdx == 35) item.setCELL35(value);
		else if(cellIdx == 36) item.setCELL36(value);
		else if(cellIdx == 37) item.setCELL37(value);
		else if(cellIdx == 38) item.setCELL38(value);
		else if(cellIdx == 39) item.setCELL39(value);
		else if(cellIdx == 40) item.setCELL40(value);
		else if(cellIdx == 41) item.setCELL41(value);
		else if(cellIdx == 42) item.setCELL42(value);
		else if(cellIdx == 43) item.setCELL43(value);
		else if(cellIdx == 44) item.setCELL44(value);
		else if(cellIdx == 45) item.setCELL45(value);
		else if(cellIdx == 46) item.setCELL46(value);
		else if(cellIdx == 47) item.setCELL47(value);
		else if(cellIdx == 48) item.setCELL48(value);
		else if(cellIdx == 49) item.setCELL49(value);
		else if(cellIdx == 50) item.setCELL50(value);
		else if(cellIdx == 51) item.setCELL51(value);
		else if(cellIdx == 52) item.setCELL52(value);
		else if(cellIdx == 53) item.setCELL53(value);
		else if(cellIdx == 54) item.setCELL54(value);
		else if(cellIdx == 55) item.setCELL55(value);
		else if(cellIdx == 56) item.setCELL56(value);
		else if(cellIdx == 57) item.setCELL57(value);
		else if(cellIdx == 58) item.setCELL58(value);
		else if(cellIdx == 59) item.setCELL59(value);
		else if(cellIdx == 60) item.setCELL60(value);
	}
}
