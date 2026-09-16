package com.tomatosystem.core.service;
import java.util.List;
import java.util.Map;

import com.cleopatra.protocol.data.DataResponse;
import com.tomatosystem.core.vo.ExcelVO;



public class ExcelImportTmpService implements ExcelRowHandleService {

	@Override
	public <T> int handle(Map paramMap, List<T> rowList, DataResponse res) throws Exception {
		if (!rowList.isEmpty()) {
			if (rowList.get(0) instanceof ExcelVO) {
				// ExcelVO 처리
				// 업무단 후처리 수행(업무적인 로직 수행을 하면서 처리하는 경우)
//				Map<String, String> mapParam = new HashMap<String, String>();
				for(T xls : rowList){
					System.out.println(((ExcelVO) xls).getCELL0()+"\t"
							+((ExcelVO) xls).getCELL1()+"\t"+
							((ExcelVO) xls).getCELL2()+"\t"+
							((ExcelVO) xls).getCELL3()+"\t"+
							((ExcelVO) xls).getCELL4()+"\t"+
							((ExcelVO) xls).getCELL5()+"\t"+
							((ExcelVO) xls).getCELL6()+"\t"+
							((ExcelVO) xls).getCELL7()+"\t"+
							((ExcelVO) xls).getCELL8()+"\t"+
							((ExcelVO) xls).getCELL9()+"\t"+
							((ExcelVO) xls).getCELL10());
				}
			} else if (rowList.get(0) instanceof Map) {
//				// 업무단 후처리 수행(업무적인 로직 수행을 하면서 처리하는 경우)
				for (T row : rowList) {
					res.send(row);
					res.flush();
				}
			}
		}

		return 0;
	}

}
