package zxjt.intfc.test.trade.ptyw;

import java.util.Map;

import javax.annotation.Resource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import zxjt.intfc.common.bean.BeforeClassUse;
import zxjt.intfc.common.testng.TestDataProvider;
import zxjt.intfc.service.trade.ptyw.A00LoginService;
import zxjt.intfc.test.common.BaseController;

//自己的类名
public class A00LoginTest extends BaseController {

	// *自己的Service名称
	@Resource
	private A00LoginService loginService;
	
//	@Resource
//	private TestReportService tr;	

	@BeforeClass
	public void setup() {
		BeforeClassUse.setDPInfo(loginService);
	}

	@Test(dataProvider = "testData", dataProviderClass = TestDataProvider.class)
	public void A00(Map<String, String> param) {

		loginService.test(param);
	}
}
