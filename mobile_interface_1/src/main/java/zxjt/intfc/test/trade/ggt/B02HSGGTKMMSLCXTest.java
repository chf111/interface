package zxjt.intfc.test.trade.ggt;

import java.util.Map;

import javax.annotation.Resource;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import zxjt.intfc.common.bean.BeforeClassUse;
import zxjt.intfc.common.testng.TestDataProvider;
import zxjt.intfc.service.trade.ggt.B02HSGGTKMMSLCXService;
import zxjt.intfc.test.common.BaseController;

//自己的类名
public class B02HSGGTKMMSLCXTest extends BaseController {

	// *自己的Service名称
	@Resource
	private B02HSGGTKMMSLCXService hsggtService;

	@BeforeClass
	public void setup() {
		BeforeClassUse.setDPInfo(hsggtService);
	}

	@Test(dataProvider = "testData", dataProviderClass = TestDataProvider.class)
	public void B02(Map<String, String> param) {

		hsggtService.test(param);
	}
}
