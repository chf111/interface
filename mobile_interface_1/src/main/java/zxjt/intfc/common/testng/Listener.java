package zxjt.intfc.common.testng;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.Test;
import org.testng.internal.IResultListener2;

import zxjt.intfc.common.bean.BeforeClassUse;
import zxjt.intfc.common.bean.SYSBean;
import zxjt.intfc.common.constant.ParamConstant;
import zxjt.intfc.common.util.CommonToolsUtil;
import zxjt.intfc.common.util.JsonAssertUtil;
import zxjt.intfc.common.util.StringUtil;
import zxjt.intfc.entity.common.StepReport;
import zxjt.intfc.entity.common.TestReport;
import zxjt.intfc.service.common.ReportService;

public class Listener implements IResultListener2 {

	private String namePath = "name.json";
	Map<String, String> nameMap = new HashMap<>();
	private String currentName;
	private ReportService reps;
	private TestReport tr;
	private StepReport sr;

	{
		SYSBean.putSysData(ParamConstant.REPORT_DATE, CommonToolsUtil.getToday("yyyyMMdd HH:mm:ss:S"));

	}

	private void setReportRep() {

		reps = (ReportService) BeforeClassUse.getReportInfo();
	}

	@Override
	public void onStart(ITestContext context) {
//		System.out.println("onStart*********");
		nameMap = JsonAssertUtil.parseJson2(namePath);

	}

	@Override
	public void onFinish(ITestContext context) {
//		System.out.println("onFinish*********");
	}

	@Override
	public void onTestStart(ITestResult result) {
//		System.out.println("onTestStart");
		if (!isReportTest(result)) {
			createOrUseExistTest(result);
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
//		System.out.println("onTestSuccess");

		if (!isReportTest(result)) {
			String paramStr, resultStr = null;
			paramStr = getParamsDescription(result);

			// 写入数据库
			addStepAndUpdateTest(result, paramStr, resultStr, null, null);
		} else {
			setReportRep();
		}

	}

	@Override
	public void onTestFailure(ITestResult result) {
		if (!isReportTest(result)) {
			String paramStr, resultStr;

			paramStr = getParamsDescription(result);

			// 生成错误信息
			Throwable th = result.getThrowable();
			resultStr = getThrowableDescription(th);
			String stackTrace = getStackTrace(th);

			// 写入数据库
			addStepAndUpdateTest(result, paramStr, resultStr, stackTrace, null);
		} else {
			throw new RuntimeException("操作report数据库失败（Failure），请查证后再试");
		}

	}

	@Override
	public void onTestSkipped(ITestResult result) {
		if (!isReportTest(result)) {
			Throwable th = result.getThrowable();
			String resultStr = null, stackTrace = null;
			if (th != null) {
				// 因DataProvider异常而被跳过时不执行onTestStart
				// 所以需要在此调用createOrUseExistTest方法
				createOrUseExistTest(result);

				// 生成错误信息
				resultStr = getThrowableDescription(th);
				stackTrace = getStackTrace(th);
			}
			String paramStr = getParamsDescription(result);
			addStepAndUpdateTest(result, paramStr, resultStr, stackTrace, null);
		} else {
			throw new RuntimeException("操作report数据库失败（Skipped），请查证后再试");
		}
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		// not used
	}

	@Override
	public void beforeConfiguration(ITestResult result) {
//		System.out.println("beforeConfiguration");
	}

	@Override
	public void onConfigurationSuccess(ITestResult result) {
//		System.out.println("onConfigurationSuccess");
	}

	@Override
	public void onConfigurationFailure(ITestResult result) {

	}

	@Override
	public void onConfigurationSkip(ITestResult result) {
	}

	private Boolean isReportTest(ITestResult result) {
		DisableReportListener isReport = result.getMethod().getConstructorOrMethod().getDeclaringClass()
				.getDeclaredAnnotation(DisableReportListener.class);
		if (isReport != null) {
			return true;
		} else {
			return false;

		}
	}

	private void createOrUseExistTest(ITestResult result) {
		Method method = result.getMethod().getConstructorOrMethod().getMethod();
		String vClassName = method.getDeclaringClass().getSimpleName();
		String vMethodName = method.getName();
		String name = vClassName + "_" + vMethodName;
		if (Test.class == getMethodType(method)) {
			if (!name.equals(currentName)) {
				// 创建新的Test
				tr = new TestReport();
				String tName = nameMap.get(name);
				tr.setName(tName);
				tr.setStarttime(CommonToolsUtil.getToday("yyyyMMdd HH:mm:ss:S"));
				tr.setFlg("test");
				tr.setReporttime(SYSBean.getSysData(ParamConstant.REPORT_DATE));
				TestReport trn = reps.saveTestInfo(tr);
				tr.setId(trn.getId());
				currentName = name;
			}
			createStepInstance();
		}
	}

	private Class<? extends Annotation> getMethodType(Method method) {
		Class<? extends Annotation> methodType = null;
		// Method method = result.getMethod().getConstructorOrMethod().getMethod();
		Annotation[] annotations = method.getAnnotations();
		for (Annotation annotation : annotations) {
			// 获取注解的具体类型
			Class<? extends Annotation> annotationType = annotation.annotationType();

			if (Test.class == annotationType) {
				// System.out.println("annotationType.getName()"+annotationType.getName());
				return annotationType;
			}
		}
		return methodType;

	}

	/*
	 * 创建Step实例
	 */
	private void createStepInstance() {
		sr = new StepReport();
		sr.setTestId(tr.getId());
		sr.setTimeStamp(CommonToolsUtil.getToday("yyyyMMdd HH:mm:ss:S"));
	}

	/*
	 * 将Step写入数据库并更新Test
	 */
	private void addStepAndUpdateTest(ITestResult result, String paramStr, String resultStr, String stack,
			String screen) {
		Method method = result.getMethod().getConstructorOrMethod().getMethod();

		if (Test.class == getMethodType(method)) {
			tr.setEndtime(CommonToolsUtil.getToday("yyyyMMdd HH:mm:ss:S"));
			reps.saveTestInfo(tr);
			sr.setStatus(String.valueOf(result.getStatus()));
			sr.setParam(paramStr);
			sr.setResult(resultStr);
			sr.setStackTrace(stack);
			sr.setScreenshot(screen);
			sr.setFlg("step");
			sr.setReporttime(SYSBean.getSysData(ParamConstant.REPORT_DATE));
			StepReport srn = reps.saveStepInfo(sr);
			sr.setId(srn.getId());
			printResult(result);
			System.out.println("test over**************************");
		}

	}

	/*
	 * 控制台打印结果
	 */
	private void printResult(ITestResult result) {
		String resultStr;
		switch (result.getStatus()) {
		case ITestResult.SUCCESS:
			resultStr = "PASS";
			break;
		case ITestResult.FAILURE:
			// System.err.println(currentName + ", FAIL\n" +
			// getThrowableDescription(result.getThrowable()));
			System.err.println(nameMap.get(currentName) + ", FAIL\n");
			return;
		case ITestResult.SKIP:
			resultStr = "SKIP";
			break;
		default:
			resultStr = "unknown status: " + result.getStatus();
			break;
		}
		// System.out.println(currentName + ", " + resultStr);
		System.out.println(nameMap.get(currentName) + ", " + resultStr);
	}

	/*
	 * 生成异常简要描述信息
	 */
	private String getThrowableDescription(Throwable tr) {
		String msg = tr.getMessage();
		if (StringUtil.hasText(msg)) {
			return StringEscapeUtils.escapeHtml4(msg);
		}
		return tr.getClass().getName();
	}

	/*
	 * 生成异常堆栈信息
	 */
	private String getStackTrace(Throwable tr) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		tr.printStackTrace(pw);
		pw.close();
		String ex = sw.toString();
		return StringEscapeUtils.escapeHtml4(ex);
	}

	/*
	 * 根据参数生成参数信息
	 */
	private String getParamsDescription(ITestResult result) {
		StringBuilder ret = new StringBuilder();
		Method method = result.getMethod().getConstructorOrMethod().getMethod();
		Parameter[] params = method.getParameters();
		Object[] values = result.getParameters();

		if (values.length == 0)
			return method.toString();

		for (int i = 0; i < params.length; ++i) {

			ret.append(params[i]).append(' ');

			ret.append(values[i]).append('\n');
		}
		if (ret.length() > 0) {
			// 删除最后一个换行
			ret.deleteCharAt(ret.length() - 1);
		}
		return ret.toString();
	}

}
