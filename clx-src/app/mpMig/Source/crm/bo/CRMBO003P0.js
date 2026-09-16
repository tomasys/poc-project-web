/******************************************************************************
* FormID(명)  : CRMBO000PO.xml
* Form 설명   : 확인자리스트 팝업
* 작성자      : 손호진
* 작성일      : 2012-07-27
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
//<trance> #include "script::import.js";					//엑셀다운로드
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
var x_clsf_sctcd; 		//  파라미터 : 분류구분코드
var x_reso_id; 			// 	파라미터 : 자원ID

//화면 처리에 필요한 변수
var x_UserId;		// 사용자ID
var x_UserNm;		// 사용자명

// txId
var tx_CheckUserList = "확인자리스트조회";

//-------------------------------------------------------------
// Form Onload 이벤트 발생 (필수)
//   처리영역 :  Form Onload 이벤트 발생시 함수호출 
//   주요내용 :  화면을 초기화
//-------------------------------------------------------------
function CRMBO000P0_OnLoadCompleted(obj)
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
	// 화면 초기화 공통함수 호출
	var category = Array(0);
	s_InitForm(category, this);
		
	x_UserId 		= s_GetCrmUser("usrid");	// 사용자ID
	x_UserNm		= s_GetCrmUser("nam");		// 사용자명
	

	// 확인자조회
	f_GetUserList();
}

/*******************************************************************************
* 사용자 함수 정의
*   - Form 의 사용자 이벤트 에서 Called 되는 Function 정의 영역
*   - Component 이벤트 Called 되는 Function  내역을 정의
*******************************************************************************/
//---------------------------------------------------------------
// Call Back 사용자 정의
//   처리영역 : Call Back
//   주요내용 : Call Back 처리후 사용자 정의 내용
//---------------------------------------------------------------
function f_DoPost(txId)
{
	if (txId == tx_CheckUserList) {
		s_OffProgressBar();
		if(ds_Checklist.rowcount < 1){
			alert("해당 게시물의 확인자가 존재하지 않습니다.");
		}
	}
}

//---------------------------------------------------------------
// 확인자목록 조회
//   처리영역 : 폼 로드시 call
//   주요내용 : 해당게시물의 확인자 목록을 조회
//---------------------------------------------------------------
function f_GetUserList()					
{	
	s_OnProgressBar();
	
	var txCode = "CRMBO001O004";			// 거래코드
	ds_Checklist.ClearData();					// 조회 전 Dataset 초기화
	
	var outDatasets = Array();				// Output Dataset
	outDatasets[0] = ds_Checklist.ID;
	
	var variables = Array(2);				// 입력 파라미터 Array
	variables[0] = "ip_clsf_sctcd,"		+ quote(x_clsf_sctcd);
	variables[1] = "ip_reso_id,"		+ quote(x_reso_id);
			
	// 조회 서비스 호출
	s_SendRequestEx(tx_CheckUserList, , txCode, , outDatasets, variables, true);
}

/*******************************************************************************
* 함수 사용자 정의
*   - Form 의 사용자 이벤트 에서 Called 되는 Function 정의 영역
*   - Component 이벤트 Called 되는 Function  내역을 정의
*******************************************************************************/
//------------------------------------------------------------
// 닫기버튼 이벤트 처리
//------------------------------------------------------------
function btn_Exit_OnClick(obj)
{
	s_CloseUp(); 
}

