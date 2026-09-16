/******************************************************************************
* FormID(명)  : CRMBO002M0
* Form 설명   : 메시지관리
* 작성자      : 김현철
* 작성일      : 2012-08-30
* 변경로그    : 
******************************************************************************/

//-------------------------------------------------------------
// API 공통함수 선언(해당 라이브러리만 선언)
//-------------------------------------------------------------
//<trance> #include "script::common.js"					// 공통처리모듈
//<trance> #include "script::comMsg.js";					// 클라이언트 메시지 정의
//<trance> #include "script::component.js";				// Component 공통기능을 처리하기 위한 공통처리모듈
//<trance> #include "script::init_loading.js";				// 초기 로딩 처리모듈
//<trance> #include "script::io_service.js";				// Tranction 관련 공통기능을 처리하기 위한 공통처리모듈
//<trance> #include "bsfScript::bsfCommonScript.js";		// 관리 공통
//<trance> #include "bsfScript::bsfVariables.js";          // 관리전용 상수/변수 정의
//<trance> #include "bsfScript::bsfAuthScript.js";			// 관리전용 권한
//<trance> #include "bsfScript::bsfCtScript.js";			//추심단별 직원조회
//<trance> #include "bsfScript::bsfComMsg.js";				//error message
//<trance> #include "script::grid.js";						//그리드헤더 클릭시 Sorting 처리

//-------------------------------------------------------------
// BCG 업무 API 공통함수 선언
//-------------------------------------------------------------
//<trance> #include "crmScript::common.js";

//-------------------------------------------------------------
// 폼 전역변수 선언
//-------------------------------------------------------------
// txId
var tx_InquireMsg 		= "메시지조회";
var tx_InquireMsgDetail = "대상자조회";
var tx_InquireAgnet  	= "상담원조회";

//화면 처리에 필요한 변수
var x_UserId;		// 사용자ID
var x_UserNm;		// 사용자명

/*******************************************************************************
* Form Onload 이벤트 발생 (필수)
*  처리영역 :  Form Onload 이벤트 발생시 함수호출 
*  주요내용 : 제수입금 상세 내역을 조회함.
*******************************************************************************/
function CRMBO002M0_OnLoadCompleted(obj)
{
	f_CommonInitForm();
}

//-------------------------------------------------------------
// Form 초기화 (필수)
//   처리영역 : Form 의 Onload 이벤트에서 Call
//   처리내용 : 초기화에 따른 해당 Form에서 처리 해야 할 사항
//-------------------------------------------------------------
function f_CommonInitForm()
{
	// 초기화 전 처리	
	var category = Array(0);
	s_InitForm(category, this); 
	
	x_UserId 		= s_GetCrmUser("usrid");	// 사용자ID
	x_UserNm		= s_GetCrmUser("nam");		// 사용자명
	
	// 콜센터 공통코드 로딩
	var x_GroupCode = Array(1);
	x_GroupCode[0]  = "TM005,cob_SrcUserGroup,A";
	s_GetCallCommonCode(x_GroupCode, this);	
	
	// 초기값 Today설정
	var x_Today 	= Today();
	cal_Start.Value = AddDate(x_Today,-7);
	cal_End.Value 	= x_Today;
	
	//초기 상담원 조회
	f_AgentListLoad();
}

/*******************************************************************************
* 이벤트 공통 버튼 처리 부분
*  처리영역 : Form 의 공통 이벤트 처리 영역  
*  주용내용 : 기능 내역 정의
*******************************************************************************/
//---------------------------------------------------------------
// 검색 조회 버튼
//   처리영역 : btn_Inquire_OnClick
//   주요내용 : 검색 조건에 확인후 검색
//---------------------------------------------------------------
function btn_Inquire_OnClick(obj)
{
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
	if(txId == tx_InquireMsg){		
		s_OffProgressBar();		
		ds_MsgList.row = 0;
			
	}else if(txId == tx_InquireMsgDetail){		
		s_OffProgressBar();
		
	}else if(txId == tx_InquireAgnet){
		//s_OffProgressBar();
		cob_Agent.Value = "";
		//f_CommonInquire();
	}
}

//---------------------------------------------------------------
// cob_User 상담원 조회
//   처리영역 : 화면 Load 
//   주요내용 : agent 조회 cob_User bindDataSet
//---------------------------------------------------------------
function f_AgentListLoad()
{
	ds_AgentLIst.ClearData();					// 조회 전 Dataset 초기화
	
	var txId		= tx_InquireAgnet;
	var txCode 		= "CRMTM003O003";			// 거래코드
	
	var outDs 		= Array(1);					// Output Dataset
	outDs[0] 		= ds_AgentLIst.ID;
	
	var variables 	= Array(1);					// 입력 파라미터 Array	
	variables[0] 	= "ip_teamcd," + quote(cob_SrcUserGroup.Value);	
	
	//진행상황 POPUP화면을 Open한다 
	//s_OnProgressBar();
	
	// 조회 서비스 호출
	s_SendRequestEx(txId, , txCode, , outDs, variables, true);
}

//---------------------------------------------------------------
// 메시지조회
//*  처리영역 : Form 의 공통 이벤트 처리 영역  
//*  주용내용 : 기능 내역 정의
//---------------------------------------------------------------
function f_CommonInquire()
{
	ds_MsgList.ClearData();
	ds_DetailList.ClearData();
	
	//진행상황 POPUP화면을 Open한다 
	s_OnProgressBar();
	
	var txId   		= tx_InquireMsg;	// txId
	var txCode 		= "CRMBO002O001";	// 거래코드
	
	var outDs 		= Array(1);			// Output Dataset
	outDs[0] 		= ds_MsgList.ID;	
	
	var variables 	= Array(4);			// 입력 파라미터 Array
	variables[0] 	= "ip_stcal,"		+ quote(cal_Start.Value);
	variables[1] 	= "ip_encal,"		+ quote(cal_End.Value);
	variables[2] 	= "ip_user_group,"	+ quote(cob_SrcUserGroup.Value);
	variables[3] 	= "ip_agent_id," 	+ quote(cob_Agent.Value);	
		
	// 조회 서비스 호출
	s_SendRequestEx(txId, , txCode, , outDs, variables, true);
}

//--------------------------------------------------------------
// 메시지대상자 목록 조회
//   처리영역 : 메시지 선택시 호출
//   주요내용 : 해당 메시지의 대상자를 조회
//--------------------------------------------------------------
function f_GetMsgSubject()
{
	//진행상황 POPUP화면을 Open한다 
	s_OnProgressBar();
	
	var x_MsgId		= ds_MsgList.GetColumn(ds_MsgList.rowposition,"notc_msg_id");
	
	var txId   		= tx_InquireMsgDetail;	// txId
	var txCode 		= "CRMBO002O002";		// 거래코드
	
	var outDs 		= Array(1);				// Output Dataset
	outDs[0] 		= ds_DetailList.ID;	
	
	var variables 	= Array(1);				// 입력 파라미터 Array
	variables[0] 	= "ip_msg_id,"			+ quote(x_MsgId);
		
	// 조회 서비스 호출
	s_SendRequestEx(txId, , txCode, , outDs, variables, true);
}


/*******************************************************************************
* 버튼 이외의 이벤트 정의
*  처리영역 : Form 의 공통 이벤트 처리 영역  
*  주용내용 : 기능 내역 정의
*******************************************************************************/
//--------------------------------------------------------------
// ds_MsgList_OnRowPosChanged
//   처리영역 : ds_MsgList OnRowPosChanged 시호출
//   주요내용 : 해당 메시지의 대상자를 조회
//--------------------------------------------------------------
function ds_MsgList_OnRowPosChanged(obj,nOldRow,nRow)
{
	ds_DetailList.ClearData();
	
	if(nRow < 0){
		return;
	}
	
	f_GetMsgSubject();
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

function cob_SrcUserGroup_OnChanged(obj,strCode,strText,nOldIndex,nNewIndex)
{
	f_AgentListLoad();
}
