package zxjt.intfc.common.testng;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class CssHelper {

	public static String getCSS() {
		try (BufferedReader reader = new BufferedReader(
				new InputStreamReader(CssHelper.class.getResourceAsStream("/report-style/custom.css")));) {
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
