/******************************************************************************
* FormID(명)  : CRMBO001MO
* Form 설명   : 업무자료실
* 작성자      : 손호진
* 작성일      : 2012-08-06
* 변경로그    : 
******************************************************************************/

//-------------------------------------------------------------
// API 공통함수 선언(해당 라이브러리만 선언)
//-------------------------------------------------------------
//<trance> #include "script::common.js"					// 공통처리모듈
//<trance> #include "script::comMsg.js";					// 클라이언트 메세지 정의
//<trance> #include "script::component.js";				// Component 공통기능을 처리하기 위한 공통처리모듈
//<trance> #include "script::init_loading.js";				// 초기 로딩 처리모듈
//<trance> #include "script::io_service.js";				// Tranction 관련 공통기능을 처리하기 위한 공통처리모듈
//<trance> #include "bsfScript::bsfCommonScript.js";		// 관리 공통
//<trance> #include "bsfScript::bsfVariables.js";          // 관리전용 상수/변수 정의
//<trance> #include "bsfScript::bsfAuthScript.js";			// 관리전용 권한
//<trance> #include "bsfScript::bsfCtScript.js";			//추심단별 직원조회
//<trance> #include "bsfScript::bsfComMsg.js";				//error message
//<trance> #include "script::grid.js";						//그리드헤더 클릭시 Sorting 처리
//<trance> #include "script::upload.js";					// 업로드 관련
//<trance> #include "script::ext_component.js";

//-------------------------------------------------------------
// CRM 업무 API 공통함수 선언
//-------------------------------------------------------------
//<trance> #include "crmScript::common.js";

//-------------------------------------------------------------
// 폼 전역변수 선언
//-------------------------------------------------------------
// txId
var tx_InquireServey 	= "업무자료실조회";
var tx_SaveServey 		= "업무자료실등록";
var tx_DeleteServey 	= "업무자료실삭제";
var tx_CheckServey 		= "업무자료실확인";
var tx_GetFileInfo		= "첨부파일정보조회";
var tx_DeleteFile 		= "첨부파일삭제";

//화면 처리에 필요한 변수
var x_UserId;		// 사용자ID
var x_UserNm;		// 사용자명

/*******************************************************************************
* Form Onload 이벤트 발생 (필수)
*  처리영역 :  Form Onload 이벤트 발생시 함수호출 
*  주요내용 : 업무자료실  내역을 조회함.
*******************************************************************************/
function CRMBO001M0_OnLoadCompleted(obj)
{
    //사용자 정의 폼 초기화 Call
	f_CommonInitForm();
}

//-------------------------------------------------------------
// Form 초기화 (필수)
//   처리영역 : Form 의 Onload 이벤트에서 Call
//   처리내용 : 초기화에 따른 해당 Form에서 처리 해야 할 사항
//-------------------------------------------------------------
function f_CommonInitForm()
{
	// Form 관련 Page Loading
	var category = Array(0);	
	s_InitForm(category, this);
		
	x_UserId 		= s_GetCrmUser("usrid");	// 사용자ID
	x_UserNm		= s_GetCrmUser("nam");		// 사용자명
	

	// 콜센터 공통코드 로딩
	var x_GroupCode = Array(1);
	x_GroupCode[0] = "KM002,cob_SearchType,B";
	s_GetCallCommonCode(x_GroupCode, this);	
	
	// 캘린더 초기값 Today설정
	var x_Today 	= Today();
	cal_Start.Value = AddDate(x_Today,-7);
	cal_End.Value 	= x_Today;
	
	// 콤보 초기값 설정
	cob_SearchType.Index = 0;
	
	// 초기조회
	f_CommonInquire();
}

/*******************************************************************************
* 사용자 함수 정의
*   - Form 의 사용자 이벤트 에서 Called 되는 Function 정의 영역
*   - Component 이벤트 Called 되는 Function  내역을 정의
*******************************************************************************/
//---------------------------------------------------------------
// Call Back 사용자 정의
//   처리영역 : Call Back
//   주요내용 : Call Back 처리후 사용자 정의 내용.
//---------------------------------------------------------------
function f_DoPost(txId)
{
	if(txId == tx_InquireServey){		
		s_OffProgressBar();
		
	}else if(txId == tx_SaveServey){	
		s_OffProgressBar();
		f_CommonInquire();
		
	}else if(txId == tx_DeleteServey){	
		s_FileDelete();
		s_OffProgressBar();
		f_CommonInquire();
	
	}else if(txId == tx_DeleteFile){	
		s_FileDelete();
		s_OffProgressBar();
		f_CommonInquire();
	
	}else if(txId == tx_CheckServey){	
	
	}else if(txId == tx_GetFileInfo){	
		f_setAttFileName();
		f_SetAttachButton(3);
		
		var regId 		= ds_Task.GetColumn(ds_Task.rowposition, "rgr_id");
		// 편집가능 모드
		if(regId == x_UserId){
			f_SetAttachButton(3);
			
		// 편집불가 모드
		}else{
			f_SetAttachButton(5);
		}
	}
}

//---------------------------------------------------------------
// 업무자료 조회
//   처리영역 : 폼 로드 , 수정,삭제 이벤트 발생시 call
//   주요내용 : 조회조건과 DataSet을 Service클래스로 전송.
//---------------------------------------------------------------
function f_CommonInquire()					//자료실 그리드 조회
{	
	f_SetAttachButton(1);					// 첨부관련 파일 비활성
	
	var txCode = "CRMBO001O001";			// 거래코드
	var outDatasets = Array();				// Output Dataset
	var variables = Array(4);				// 입력 파라미터 Array

	ds_Task.ClearData();					// 조회 전 Dataset 초기화
				
	// output dataset
	outDatasets[0] = ds_Task.ID;
	
	variables[0] = "ip_stcal,"		+ quote(cal_Start.Value);
	variables[1] = "ip_encal,"		+ quote(cal_End.Value);
	variables[2] = "ip_src_type,"	+ quote(cob_SearchType.Value);
	variables[3] = "ip_src_value,"	+ quote(edt_Keywords.Text);
	
	//진행상황 POPUP화면을 Open한다 
	s_OnProgressBar();
		
	// 조회 서비스 호출
	s_SendRequestEx(tx_InquireServey, , txCode, , outDatasets, variables, true);
}

//--------------------------------------------------------------
// 업무자료 저장
//   처리영역 : 저장버튼 Key Event 발생 함수에서 Call
//   주요내용 : 업무자료를 저장한다.
//--------------------------------------------------------------
function f_CommonStore()
{
	if(!f_CommonPreStore()) {
		return;
	}
	
	var x_nowRow	= ds_Task.rowposition;
	var x_nowState 	= ds_Task.GetRowType(x_nowRow);
	
	var confirmMemt = "";
	
	if(x_nowState == "insert"){
		confirmMemt = "등록";
		
	}else{
		confirmMemt = "수정";
	}
	
	if(!confirm(confirmMemt+"하시겠습니까?")){
		return;
	}
	
	s_OnProgressBar();
	
	// 첨부파일 존재시
	if(ds_Files.RowCount > 0){
		// 첨부한 파일들을 서버 디렉토리로 업로드.
		var ret = s_FileUpload();
		
		if (ret[0] == "FAIL") {       	// 파일 업로드 실패.
			alert(ret[1]);   			// 에러메시지 출력
			return;
		}
	}
		
	var txId    	= tx_SaveServey;
	var txCode  	= "CRMBO001O002";

	var inDs        = Array(1);             	
	inDs[0]     	= ds_Task.ID + ",U";		
	
	var variables   = Array(2);     
	variables[0] 	= "ip_mode,"	+ quote(x_nowState);	    	
	variables[1] 	= "ip_user_id," + quote(x_UserId);
	
	s_SendRequestEx(txId, , txCode, inDs ,  , variables);	
}

//--------------------------------------------------------------
// 업무자료 삭제
//   처리영역 : 저장버튼 Key Event 발생 함수에서 Call
//   주요내용 : 업무자료를 삭제한다.
//--------------------------------------------------------------
function f_CommonDelete()
{
	if(!confirm("삭제하시겠습니까?")){
		return;
	}
	
	s_OnProgressBar();
	
	var x_NowRow  	= ds_Task.rowposition;
	var resoId 		= ds_Task.GetColumn(x_NowRow, "bz_data_id");
	var delFileId 	= ds_Task.GetColumn(x_NowRow, "atcg_file_id");
	s_DeleteFileAttach(delFileId);		
	
	var txId    	= tx_DeleteServey;
	var txCode  	= "CRMBO001O002";

	var inDs        = null;           	
	var outDs		= null;		
	
	var variables   = Array(1);              	
	variables[0] 	= "ip_mode,'delete'";
	variables[1] 	= "ip_reso_id,"	+ quote(resoId);
	variables[2] 	= "ip_file_id,"	+ quote(delFileId);
	
	s_SendRequestEx(txId, , txCode, inDs, outDs, variables);
}

//--------------------------------------------------------------
// 첨부파일 삭제
//   처리영역 : 첨부삭제 Key Event 발생 함수에서 Call
//   주요내용 : 첨부파일을 삭제한다.
//--------------------------------------------------------------
function f_AttachDelete()
{
	if(ds_Task.GetUpdate()){
		alert("작업중인 내용이 존재합니다. 작업완료후 계속하세요.");
		return;
	}

	if(!confirm("첨부삭제한 파일은 복구할 수 없습니다.\n\n첨부파일을 삭제하시겠습니까?")){
		return;
	}
	
	s_OnProgressBar();
	
	var x_nowRow	= ds_Task.rowposition;
	var fileID 		= ds_Task.GetColumn(ds_Task.rowposition, "atcg_file_id");	// 파일ID 
    s_DeleteFileAttach(fileID);													// ds_Files에 첨부삭제
    ds_Task.SetColumn(ds_Task.rowposition, "atcg_file_id","");
	
	var txId    	= tx_DeleteFile;
	var txCode  	= "CRMBO001O006";

	var inDs        = Array(1);             	
	inDs[0]     	= ds_Task.ID + ",U";		
	
	var variables   = Array(1);   	    	
	variables[0] 	= "ip_user_id," + quote(x_UserId);
	
	s_SendRequestEx(txId, , txCode, inDs ,  , variables);	
}


/*******************************************************************************
* 이벤트 공통 버튼 처리 부분
*  처리영역 : Form 의 공통 이벤트 처리 영역  
*  주용내용 : 기능 내역 정의
*******************************************************************************/
//--------------------------------------------------------------
//  검색버튼 클릭
//   처리영역 : btn_Search OnClick 이벤트 
//   주요내용 : 조회함수를 호출한다.
//--------------------------------------------------------------
function btn_Inquire_OnClick(obj)
{
	f_CommonInquire();
}

//--------------------------------------------------------------
//  추가버튼 클릭
//   처리영역 : btn_Add_OnClick 이벤트 
//   주요내용 : 프로그램정보를 추가한다.
//--------------------------------------------------------------
function btn_Add_OnClick(obj)
{
	if(ds_Task.GetUpdate()){
		alert("작업중인 내용이 존재합니다. 작업완료후 계속하세요.");
		return;
	}
	
	ds_Task.AddRow();
	
	edt_Title.Readonly 		= false;
	txa_Contents.Readonly 	= false;
	btn_Attach.Enable 		= true;
}

//--------------------------------------------------------------
//  삭제버튼 클릭
//   처리영역 : btn_Delete OnClick 이벤트 
//   주요내용 : 선택된 프로그램정보를 삭제한다.
//--------------------------------------------------------------
function btn_Delete_OnClick(obj)
{
	if(ds_Task.GetRowType(ds_Task.rowposition) == "insert"){
		ds_Task.DeleteRow(ds_Task.rowposition);
		return;
	}
	
	if(ds_Task.GetUpdate()){
		alert("작업중인 내용이 존재합니다. 작업완료후 계속하세요.");
		return;
	}
	
	var x_NowRow = ds_Task.rowposition;
	var x_rgr_id = ds_Task.GetColumn(x_NowRow, "rgr_id");
	
	if(x_rgr_id != x_UserId){
		alert("작성자만 삭제가 가능합니다.");
		return;
	}
	
	f_CommonDelete();
}

//--------------------------------------------------------------
//  저장버튼 클릭 - 권한저장
//   처리영역 : btn_Store OnClick 이벤트 
//   주요내용 : 저장함수를 호출한다.
//--------------------------------------------------------------
function btn_Store_OnClick(obj)
{
	f_CommonStore();
}

//------------------------------------------------------------------------------
// 캘린더 버튼 클릭 Event 발생시 달력 드롭다운 처리
//------------------------------------------------------------------------------
function btn_StartCal_OnClick(obj)
{
	cal_Start.DropDown();
}

function btn_EndCal_OnClick(obj)
{
	cal_End.DropDown();
}
	
//--------------------------------------------------------------
//  첨부파일 첨부버튼 클릭
//   처리영역 : btn_Attach OnClick 이벤트 
//   주요내용 : 업무자료실 내용에 파일을 첨부한다.
//--------------------------------------------------------------
function btn_Attach_OnClick(obj)		//업무자료실 첨부파일을 추가
{	
	// 파일 선택 창 출력.
	var fileId = s_FileAttach();
	
	if(fileId >= 0) 
	{
		ds_Task.SetColumn(ds_Task.rowposition, "atcg_file_id", fileId); 	// 파일아이디 추가
		edt_AttachFileInfo.Text = s_GetLocalFilePath(fileId);
		
		// 첨부 파일 유형을 설정한다.
		s_SetFileType(fileId, TYPE_GENERAL);      // CASE 1 : 일반 파일
		
		f_SetAttachButton(4);
	}
}	

//--------------------------------------------------------------
//  첨부파일 취소버튼 클릭
//   처리영역 : btn_CancelAttach OnClick 이벤트 
//   주요내용 : 업무자료실 내용에 첨부한 파일을 첨부취소한다.
//--------------------------------------------------------------
function btn_CancelAttach_OnClick(obj)
{
	var fileId = ds_Task.GetColumn(ds_Task.rowposition, "atcg_file_id");	// 파일ID 
	s_CancelFileAttach(fileId);												// ds_Files에 첨부취소
	ds_Task.SetColumn(ds_Task.rowposition, "atcg_file_id","");				// 첨부파일ID 초기화
	f_SetAttachButton(2);
	f_setAttFileName();
}

//--------------------------------------------------------------
//  첨부파일 삭제버튼 클릭
//   처리영역 : btn_DelFile OnClick 이벤트 
//   주요내용 : 업무자료실 내용중에 첨부되어 있는 파일을 삭제, 저장한다.
//--------------------------------------------------------------
function btn_DelFile_OnClick(obj)
{
	f_AttachDelete();
}

//--------------------------------------------------------------
//  첨부파일 다운로드버튼 클릭
//   처리영역 : btn_TaskCancel OnClick 이벤트 
//   주요내용 : 업무자료실 내용중에 첨부되어 있는 파일을 다운로드 한다.
//--------------------------------------------------------------
function btn_FileDownload_OnClick(obj)
{
	// 개발자 정의 첨부파일 데이터 셋에 포함되어 있는 File ID 획득.
	var fileId = ds_Task.GetColumn(ds_Task.rowposition, "atcg_file_id"); 
	
	if(fileId == null || fileId == ""){
		alert("첨부파일이 존재하지 않습니다.");
		return;
	}
	
	// 파일 다운로드 공통함수 호출.
	var ret = s_FileDownload(fileId);
	
	if (ret[0] == "FAIL")
	{
		alert(ret[1]);
		return;	
		
	}
	else if(ret[0] == "SUCC"){
		alert("파일 다운로드 성공!!");
	}
}

//---------------------------------------------------------------
// 저장전
//   처리영역 : 저장 공통 정의 함수에서 call
//   주요내용 : 필수 입력사항을 입력하였는지 체크
//---------------------------------------------------------------
function f_CommonPreStore()
{	
	var result = false;	
	
	if(!ds_Task.GetUpdate()){
		alert("변경사항이 존재하지 않습니다.");	
		return result;
	}
	
	if(ds_Task.GetDelRowCount() > 0){
		if(Confirm("삭제된 데이터가 존재합니다. 계속진행하시겠습니까?"));
	}
	
	var category = Array();
	//그룹코드 입력항목 체크
	category[0] =  "titl,mt_ctt";
	category[1] =  "'제목','내용'";
	result = !s_Validation("2", category, ds_Task.ID);

	return result;
}

//---------------------------------------------------------------
// ds_Task_OnRowPosChanged
//   처리영역 : ds_Task의 줄바꿈시 Call
//   주요내용 : 줄발꿈시 권한에 따른 화면 설정
//---------------------------------------------------------------
function ds_Task_OnRowPosChanged(obj,nOldRow,nRow)
{
	ds_Files.ClearData();
	edt_AttachFileInfo.Text = "";
	
	if(nRow < 0 ){
		return;
	}
	
	var x_NowRow 		= ds_Task.rowposition;
	var x_rgr_id 		= ds_Task.GetColumn(x_NowRow, "rgr_id");
	
	// 편집가능 모드
	if(x_rgr_id == x_UserId){
		edt_Title.Readonly 		= false;
		txa_Contents.Readonly 	= false;
		
	// 편집불가 모드
	}else{
		edt_Title.Readonly 		= true;
		txa_Contents.Readonly 	= true;
	}
	
	var x_nowState = ds_Task.GetRowType(ds_Task.rowposition);
	if(x_nowState == "insert"){
		f_SetAttachButton(2);
	}else{
		f_CheckOpen();		// 확인자 등록
		f_GetFileInfo();	// 첨부파일정보 조회
	}
}

//---------------------------------------------------------------
// 게시물 확인정보 저장
//   처리영역 : ds_Task의 줄바꿈시 Call
//   주요내용 : 확인할때 확인자 정보저장
//---------------------------------------------------------------
function f_CheckOpen()
{
	var x_ResoId 	= ds_Task.GetColumn(ds_Task.rowposition, "bz_data_id");
	var x_UserIp	= split(ext_GetIPAddress(),",");
	
	x_UserIp[0] = Replace(x_UserIp[0],"[","");
	x_UserIp[0] = Replace(x_UserIp[0],"]","");
	
	var txId    	= tx_CheckServey;
	var txCode  	= "CRMBO001O003";

	var inDs        = null;          	
	var outDs       = null;
	  
	var variables   = Array(4);  
	variables[0] 	= "ip_clsf_sctcd,'2'";	
	variables[1] 	= "ip_reso_id," + quote(x_ResoId);
	variables[2] 	= "ip_user_id," + quote(x_UserId);
	variables[3] 	= "ip_inq_ip,"  + quote(x_UserIp[0]);

	s_SendRequestEx(txId, , txCode, inDs, outDs, variables);
}

//---------------------------------------------------------------
// 게시물 첨부파일 정보조회
//   처리영역 : ds_Task의 줄바꿈시 Call
//   주요내용 : 첨부파일 정보조회
//---------------------------------------------------------------
function f_GetFileInfo()
{
	var x_nowRow	= ds_Task.rowposition;
	var x_FileId 	= ds_Task.GetColumn(x_nowRow, "atcg_file_id");
	var x_ResoId 	= ds_Task.GetColumn(x_nowRow, "bz_data_id");
	var x_rgr_id 	= ds_Task.GetColumn(x_nowRow, "rgr_id");
	
	if(x_FileId == null || x_FileId == ""){
	
		// 편집가능 모드
		if(x_rgr_id == x_UserId){
			f_SetAttachButton(2);
			
		// 편집불가 모드
		}else{
			f_SetAttachButton(1);
		}
				
		return;		
	}
	
	var txId    	= tx_GetFileInfo;
	var txCode  	= "CRMBO001O005";

	var inDs        = null;          	
	var outDs       = null;   
	
	var variables   = Array(1);              	
	variables[0] 	= "ip_reso_id," + quote(x_ResoId);

	s_SendRequestEx(txId, , txCode, inDs, outDs, variables);
}

//---------------------------------------------------------------
// grd_TaskData OnCellDblClick
//   처리영역 : 확인자수 더블클릭시 호출
//   주요내용 : 확인자 목록 팝업 오픈
//---------------------------------------------------------------
function grd_TaskData_OnCellDblClick(obj,nRow,nCell,nX,nY,nPivotIndex)
{
	if(nCell == 4){
		var x_ResoId 	= ds_Task.GetColumn(ds_Task.rowposition, "bz_data_id");
	
		var popType	= "Open";
		var url		= "crmBo::CRMBO000P0.xml";
		var Param	= "x_clsf_sctcd='2'"
		            + " x_reso_id='" + x_ResoId + "'";
		var nWidth	= 420;
		var nHeight	= 320;
	
		var retValue = s_GetPopUpLoad(popType, Url, Param, nWidth, nHeight); // Popup에서 변수(배열)로 준다.
	}
}

//---------------------------------------------------------------
// 업무자료실목록 이동선택 전
//   처리영역 : ds_Task CanRowPosChange에서 Event 호출
//   주요내용 : 
//---------------------------------------------------------------
function ds_Task_CanRowPosChange(obj,nOldRow,nRow)
{
	if(ds_Task.GetUpdate()){
		alert("작업중인 내용이 존재합니다. 작업완료후 계속하세요.");
		return false;
	}
}

//---------------------------------------------------------------
// 첨부관련 버턴 설정
//   처리영역 : row 상태와 현재 파일첨부여부에 따라 변경설정
//   주요내용 : 첨부, 취소, 삭제, 다운로드 Enable 설정
//---------------------------------------------------------------
function f_SetAttachButton(mode)
{
	switch(mode)
	{
		case 1 : 
			btn_Attach.Enable 		= false;
			btn_CancelAttach.Enable = false;
			btn_DelFile.Enable 		= false;
			btn_FileDownload.Enable	= false;
			break;
		
		case 2 : 
			btn_Attach.Enable 		= true;
			btn_CancelAttach.Enable = false;
			btn_DelFile.Enable 		= false;
			btn_FileDownload.Enable	= false;
			break;
		  
		case 3 : 
			btn_Attach.Enable 		= false;
			btn_CancelAttach.Enable = false;
			btn_DelFile.Enable 		= true;
			btn_FileDownload.Enable	= true;
			break;
			
		case 4 : 
			btn_Attach.Enable 		= false;
			btn_CancelAttach.Enable = true;
			btn_DelFile.Enable 		= false;
			btn_FileDownload.Enable	= false;
			break;
		
		case 5 : 
			btn_Attach.Enable 		= false;
			btn_CancelAttach.Enable = false;
			btn_DelFile.Enable 		= false;
			btn_FileDownload.Enable	= false;
			break;
		  
		default:
			return;
	} 
}

//---------------------------------------------------------------
// 파일명 설정
//   처리영역 : 파일정보로딩, 파일첨부, 파일삭제시 호출
//   주요내용 : 첨부여부에 다른 파일명을 보여준다.
//---------------------------------------------------------------
function f_setAttFileName()
{
	var x_atcg_file_id 	= ds_Task.GetColumn(ds_Task.rowposition, "atcg_file_id");
	
	edt_AttachFileInfo.Text = "";
	if(x_atcg_file_id != null && x_atcg_file_id != ""){
		edt_AttachFileInfo.Text = s_GetFileName(x_atcg_file_id);
	}	
}

//---------------------------------------------------------------
// Form의 key Event 발생 
// 핫키 사용자 정의
//---------------------------------------------------------------



function edt_Keywords_OnKeyDown(obj,nChar,bShift,bCtrl, bAlt,LLParam,HLParam)
{
	if(nChar==13){
		f_CommonInquire();
	}
}
