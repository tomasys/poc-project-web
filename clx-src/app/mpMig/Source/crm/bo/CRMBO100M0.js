/*******************************************************************************
 * Form ID  : BTERD101M0
 * Form 설명: R&D경제성 평가
 * 작성자   : 박혁제
 * 작성일   : 2008-10-20
 * 변경로그 : 일자별로 변경자 및 변경사항을 간략히 기술
*******************************************************************************/


//------------------------------------------------------------------------------
// API 공통 함수 선언 (해당 라이브러리만 선언)
//------------------------------------------------------------------------------
//<trance> #include "script::init_loading.js"
//<trance> #include "script::io_service.js"
//<trance> #include "script::common.js"
//<trance> #include "script::comMsg.js"
//<trance> #include "script::component.js"
//<trance> #include "script::ext_component.js"
//<trance> #include "script::grid.js"
//<trance> #include "script::import.js"

//<trance> #include "bteScript::bte_common.js"


//------------------------------------------------------------------------------
// Form 전역 변수 선언
//------------------------------------------------------------------------------
//글로벌 취득값 보존을 위한 변수
var x_Cmpid;		// 고객번호
var x_TecAprlSeqnum;// 기술평가순번
var x_Cmpnm;		// 기업체명

var tx_DeleteAll = "R&D 가치평가 전체 삭제";

var x_RndLobzCd;

var ip_tab_index; 	// Active할 Index 값
var x_Result_Link = 0;

// tab화면의 url =====================================================
var x_TabUrlArr = array(8);

//==============================================================================
// Form OnLoadCompleted Event 발생 (필수)
//   처리영역: Form 초기화 함수 호출
//   주요내용: Form Loading
//==============================================================================
function BTERD101M0_OnLoadCompleted(obj)
{
	f_CommonInitForm();
}


//------------------------------------------------------------------------------
// Form 초기화 (필수)
//   처리영역: Form OnLoadCompleted Event에서 호출
//   주요내용: 진행상황 표시, 공통 코드 바인딩
//------------------------------------------------------------------------------
function f_CommonInitForm()
{
	//공통 코드를 해당 컴포넌트에 바인딩
	var category = Array(0);
	s_InitForm(category, this);
}


//------------------------------------------------------------------------------
// 초기 조회 (필수)
//   처리영역: s_InitForm 함수에서 공통 코드 처리 후 자동 호출
//   주요내용: 초기화, 조회 함수 호출
//------------------------------------------------------------------------------
function f_CommonInitInquire()
{
	//기술평가 권한 확인
	//f_TeapAutr();
	// tabpage url Settting 
	f_CommonInitTabPageUrl();
	
	//초기값 설정 (선택, 함수명 일치)
	f_CommonInitData();
	//컴포넌트 초기화(선택,함수명일치)
	f_CommonInitComponent();
	
}


//-------------------------------------------------------------
// 컴포넌트 초기화 (선택,함수명일치)
//   처리영역 : f_CommonInitInquire 함수에서 Call
//   처리내용 : 초기화에 따른 해당 컴포넌트에서 처리 해야 할 사항
//-------------------------------------------------------------
function f_CommonInitComponent()
{
	if( ip_tab_index == "" || ip_tab_index == null ) {
		ip_tab_index = 0;
	}
	tab_CmpOvacnd.TabIndex = ip_tab_index;
	if( ip_tab_index == 0 ) {
		// 화면연결 
		f_TabChanged(0);
	}

}

//------------------------------------------------------------------------------
// 초기값 설정 (선택, 함수명 일치)
//   처리영역: 초기 조회 함수에서 호출
//   주요내용: 초기화에 따른 변수, 컴포넌트 등 초기값 설정
//------------------------------------------------------------------------------
function f_CommonInitData()
{
	s_GetMainParam();
	//글로벌에서 고객번호, 기술평가순번, 기업체명, 기술평가서번호 취득
	x_Cmpid         = s_GetGlobalCust("cmpid");
	x_Cmpnm         = s_GetGlobalCust("cmp_folnm");
	
	// 평가서순번이 없을경우에만 획득..
	if( x_TecAprlSeqnum == "" || x_TecAprlSeqnum == null ) {
		x_TecAprlSeqnum = s_GetGlobalCust("tec_aprl_seqnum");
	}

	edt_Cmpid.Text = x_Cmpid;
	edt_Cmpnm.Text = x_Cmpnm;
	edt_TecApdocSeqnum.Text = x_TecAprlSeqnum;

	// 단계별 저장상태 확인버튼을 표시해준다.
	//if(RND_SAVE_PROC_ACPYMD <= parent.ds_ApdocList.GetColumn(ds_ApdocList.currow, "acpymd")) {
	//	btnSaveChk.Visible = true;
	//}
	//else {
	//	btnSaveChk.Visible = false;
	//}
	
	// R&D 가치평가 전체삭제 
	if(s_Developer(s_GetUser("usrid"))) {
		btn_DeleteAll.Enable = true;
	}
}

//------------------------------------------------------------------------------
// tabpage의 url 초기값 설정
//   처리영역: f_CommonInitData() 함수에서 호출
//   주요내용: tabpage의url을 세팅함 
//------------------------------------------------------------------------------
function f_CommonInitTabPageUrl()
{
	// tabpage url Settting ==============================================
	x_TabUrlArr[0] = "bteRd::BTERD201M0.xml";		//단계별추정기간
	x_TabUrlArr[1] = "bteRd::BTERD407M0.xml";		// 고속성장기매출액
	x_TabUrlArr[2] = "bteRd::BTERD302M0.xml";		// rnd과제 개발비
	x_TabUrlArr[3] = "bteRd::BTERD303M0.xml";		// 자본적지출
	x_TabUrlArr[4] = "bteRd::BTERD304M0.xml";		// 운전자본지출
	x_TabUrlArr[5] = "bteRd::BTERD401M0.xml";		// 고속성장기
	x_TabUrlArr[6] = "bteRd::BTERD703M0.xml";		// 안정쇠퇴기현금흐름
	x_TabUrlArr[7] = "bteRd::BTERD701M0.xml";		// 평가결과
	x_TabUrlArr[8] = "bteRd::BTERD201M1.xml";		//단계별추정기간 신양식
	x_TabUrlArr[9] = "bteRd::BTERD407M1.xml";		// 고속성장기매출액
	x_TabUrlArr[10] = "bteRd::BTERD701M1.xml";		// 평가결과
	divSaveChk.Url = "bteRd::BTERD101S0.xml";
}

//------------------------------------------------------------------------------
// Form OnKeyDown Event 발생 (필수)
//   처리영역: s_GetCommonHotKey_OnKeyDown 함수 호출
//   주요내용: 공통 Hot Key 실행
//------------------------------------------------------------------------------
function BTERD101M0_OnKeyDown(obj, objSenderObj, nChar, bShift, bControl, bAlt, nLLParam, nHLParam)
{
	s_GetCommonHotKey_OnKeyDown(obj, objSenderObj, nChar, bShift, bControl, bAlt, nLLParam, nHLParam);
}


/* ******************************************************************
*  함수 사용자 정의
*    처리영역: 함수 처리 영역
*    주요내용: 함수 사용자 정의 내용
* ****************************************************************** */
//------------------------------------------------------------------------------
// Call Back 사용자 정의
//   처리영역: Call Back
//   주요내용: Call Back 처리 후 사용자 정의 내용
//------------------------------------------------------------------------------
function f_DoPost(txId)
{
	//진행상황 표시 해제
	s_OffProgressBar();
	
	if(txId == tx_DeleteAll) {
		s_CloseUp();
	}
}

/* ******************************************************************
*  이벤트 사용자 정의(공통 버튼 이벤트 이외의 이벤트 처리)
*    처리영역: 이벤트 처리 영역
*    주요내용: 기능 내역 정의
* ****************************************************************** */

//------------------------------------------------------------------------------
//	탭화면 연결 
//   처리영역: tab changed event 에서 call
//	 처리내용: 탭페이지에 링크 연결 
//------------------------------------------------------------------------------
function f_TabChanged(idx)
{
	// 	parent.ds_ApdocList.GetColumn(ds_ApdocList.curRow, "acpymd"); 접수일자를 가져오는 문구 추가해야함.
	if(idx == 0 && RND3_APPT_ACPYMD < parent.ds_ApdocList.GetColumn(parent.ds_ApdocList.curRow, "acpymd")){
		f_GetTabItem(tab_CmpOvacnd).Url = x_TabUrlArr[8];
	}else if(idx == 1 && RND3_APPT_ACPYMD < parent.ds_ApdocList.GetColumn(parent.ds_ApdocList.curRow, "acpymd")){
		f_GetTabItem(tab_CmpOvacnd).Url = x_TabUrlArr[9];
	}else if(idx == 7 && RND3_APPT_ACPYMD < parent.ds_ApdocList.GetColumn(parent.ds_ApdocList.curRow, "acpymd")){
		f_GetTabItem(tab_CmpOvacnd).Url = x_TabUrlArr[10];
	}else{
		f_GetTabItem(tab_CmpOvacnd).Url = x_TabUrlArr[idx];
	}
	f_GetTabItem(tab_CmpOvacnd).Reload();
}

//------------------------------------------------------------------------------
//     tabpage object return 
//	param :  tabObj ( tab Object Name - (type : object ) )
//			  idx	( index	of tab 	  - (type : number ) )
//------------------------------------------------------------------------------
function f_GetTabItem(tabObj, idx)
{
	if( idx == "" || idx == null ) {
		idx = tabObj.TabIndex;
	}
	return tabObj.GetItem(idx);
}


/* ******************************************************************
*  - 공통 버튼 이외의 Component 이벤트 
*    처리영역: 컴포넌트 이벤트에서 call
*    주요내용: 공통 버튼 이외의 Component 이벤트 Called 되는 Function  내역을 정의
* ****************************************************************** */
//------------------------------------------------------------------------------
//   처리영역: 화면 종료
//------------------------------------------------------------------------------
function btn_Close_OnClick(obj)
{
	s_CloseUp();
}

//------------------------------------------------------------------------------
//   탭페이지 변경 이벤트 
//------------------------------------------------------------------------------
function tab_CmpOvacnd_OnChanged(obj,nOldIndex,nNewindex)
{
	f_TabChanged(nNewindex);
}

//------------------------------------------------------------------------------
//   탭페이지 변경전 이벤트 
//------------------------------------------------------------------------------
function tab_CmpOvacnd_OnChanging(obj,nOldIndex,nNewIndex)
{
	var result = true, vMsg;

	// 함수가 있을경우에만 호출 
	if( f_GetTabItem(obj, nOldIndex).IsExistFunc("f_PreChangingCheck") ) {
		result = f_GetTabItem(obj, nOldIndex).f_PreChangingCheck();
		if( !result ) {
			vMsg  = "<주의> 변경된 내용이 있습니다.\n";
			vMsg += "탭이동시 입력된 내용은 저장되지 않습니다.\n";
			vMsg += "그래도 이동 @common_QustConfirm";
			result = s_GetAlertMsg(  ,"confirm", vMsg);
		}
	}

	return result ;
}


//------------------------------------------------------------------------------
// 저장순서 안내 관련
//------------------------------------------------------------------------------
function btnSaveChk_OnClick(obj)
{
	if(divSaveChk.Visible == true) {
		divSaveChk.Visible = false;
	}
	else {
		f_Reload_SaveProc();
		divSaveChk.Visible = true;
	}
}

function f_Reload_SaveProc() {
	divSaveChk.f_CommonInquire();
}

//------------------------------------------------------------------------------
// R&D 업무 전체를 삭제한다.
//------------------------------------------------------------------------------
function btn_DeleteAll_OnClick(obj)
{
	f_CommonDelete();
}

//***************************************************************
// R&D 업무 전체 삭제 전처리
//***************************************************************
function f_CommonPreDelete()
{
	if(Length(Trim(ds_ApdocList.GetColumn(ds_ApdocList.currow, "cmpt_rgsymd"))) > 0) {
		s_GetAlertMsg(, "error", "이미 완료등록 되었으므로 삭제할 수 없습니다. @ ");
		return false;
	}

	if(!s_GetAlertMsg(, "confirm", "R&D 가치평가를 모두 삭제하시겠습니까? @ ")) {
		return false;
	}

	return true;
}

//***************************************************************
// R&D 업무 전체 삭제
//***************************************************************
function f_CommonDelete()
{
	if(f_CommonPreDelete()){

		s_OnProgressBar();  
		
		txCode       = "BTERD201O004"; // 작성항목 필수/선택 DBIO : BTETR101O001V001
		
		outDs        = array();

		variables    = array();
		variables[0] = "ip_cmpid," 	+ quote(x_Cmpid);		// 고객번호
		variables[1] = "ip_tec_aprl_seqnum," 	+ quote(x_TecAprlSeqnum);		// 평가순번
		
		
		s_SendRequestEx(tx_DeleteAll, , txCode,  , outDs, variables);
	}
}
