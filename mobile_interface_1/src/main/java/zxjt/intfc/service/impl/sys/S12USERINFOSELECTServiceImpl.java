package zxjt.intfc.service.impl.sys;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import zxjt.intfc.common.constant.ParamConstant;
import zxjt.intfc.common.util.CommonToolsUtil;
import zxjt.intfc.common.util.HttpUtil_All;
import zxjt.intfc.common.util.JsonAssertUtil;
import zxjt.intfc.common.util.LogUtils;
import zxjt.intfc.dao.common.AccountRepository;
import zxjt.intfc.dao.common.AddressRepository;
import zxjt.intfc.dao.sys.S12USERINFOSELECTRepository;
import zxjt.intfc.entity.sys.S12USERINFOSELECT;
import zxjt.intfc.service.sys.S12USERINFOSELECTService;

@Service
public class S12USERINFOSELECTServiceImpl implements S12USERINFOSELECTService {

	@Resource
	private S12USERINFOSELECTRepository systemDao;
	
	@Autowired
	private  AddressRepository addrDao;
	
	@Autowired
	private  AccountRepository accoDao;

	public Object[][] getParamsInfo() {
		// 股票买卖数据操作
		List<S12USERINFOSELECT> lis = systemDao.findByFunctionidAndIsExcuteIgnoreCase(ParamConstant.USERINFO_SELECT,"true");
		
		Object[][] obj = CommonToolsUtil.getWWData(lis,addrDao,accoDao,ParamConstant.USERINFO_SELECT);

		return obj;
	}

	/**
	 * 发送请求并校验返回结果
	 * 
	 * @param 入参
	 * @DependenceParam 依赖接口的入参
	 */
	public void test(Map<String, String> param) {
		try {
			Map<String, String> map = CommonToolsUtil.getRParam(param);
			LogUtils.logInfo(param.toString());
			LogUtils.logInfo(map.toString());

			String response = HttpUtil_All.doPostSSL(param.get(ParamConstant.URL), map,ParamConstant.NEED_PUT_REQ_HEADER_INFO);
			LogUtils.logInfo(response.toString());

			// 校验
			JsonAssertUtil.checkResponse(param, null, ParamConstant.S12_SCHEMA, ParamConstant.SYSTEM, response);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
