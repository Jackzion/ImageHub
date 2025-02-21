package com.ziio.imagehubbackend.api.imagesearch.sub;

import cn.hutool.core.util.URLUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.ziio.imagehubbackend.exception.BusinessException;
import com.ziio.imagehubbackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GetImagePageUrlApi {

    private static final String TOKEN =
            "1740025879759_1740100860700_MO/MYUOPVpb5B6ZVIpfl9bwJQkSGImgduDMN/iR6oR/PcyEgz6M2+jlzTJFIbeyw+abNEmvameTTIUTV6t+JpmYt11ovTfDgBe7UdmcBaEC3+fq5QMAMMoNqxFeP8UCNIX/Fou5cSC8BQcLmXCaWBLTDW1bH1a/tiGa78nlrl+xLylnT6LbB8l5C4xdOdytQvASrFdyh2TD615VcCnKFGckpU5tlZtdhnXvysGItjaYbu8oISKPQXlkaLIvf3MEG/0MO+FjVRTIeU41wzaedIQeliS2oKzpA97SX/f+aCzl/IlJaPsSDNiuE4BsWWA7SxJ680H0hGH28R9lRrzm5kROP827XVC3D0Gjb1vBI2bClrjcfYiqbmzVU3l17jTkqYG/98ZdzmhyZW2+Q9wWJ+QAiiPG8npguVPa5Ydls/gjtU/j9noYibm9IaqmAPg0oJi1Bcx7DNhLwyjBFVlEfeA==";

    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;
        // 发送请求
        try {
            // 2. 发送 POST 请求到百度接口
            HttpResponse response = HttpRequest.post(url)
                    .header("acs-token",TOKEN)
                    .form(formData)
                    .timeout(5000)
                    .execute();
            // 判断响应状态
            if (HttpStatus.HTTP_OK != response.getStatus()) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            // 解析响应
            String responseBody = response.body();
            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);

            // 3. 处理响应结果
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果 URL 为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    /**
     * 对搜索结果页面进行爬取 ， 获取 firsturl
     * @param url
     * @return
     */
    public static String getImageFirstUrl(String url){
        try {
            // 使用 Jsoup 获取 HTML 内容
            Document document = Jsoup.connect(url)
                    .timeout(5000)
                    .get();
            // 获取所有 <script> 标签
            Elements scriptElements = document.getElementsByTag("script");
            // 遍历找到包含 `firstUrl` 的脚本内容
            for (Element script : scriptElements) {
                String scriptContent = script.html();
                if (scriptContent.contains("\"firstUrl\"")) {
                    // 正则表达式提取 firstUrl 的值
                    Pattern pattern = Pattern.compile("\"firstUrl\"\\s*:\\s*\"(.*?)\"");
                    Matcher matcher = pattern.matcher(scriptContent);
                    if (matcher.find()) {
                        String firstUrl = matcher.group(1);
                        // 处理转义字符
                        firstUrl = firstUrl.replace("\\/", "/");
                        return firstUrl;
                    }
                }
            }
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未找到 url");
        } catch (IOException e) {
            log.error("搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

//    public static void main(String[] args) {
//        // 测试以图搜图功能
//        String imageUrl = "https://www.lyrical-nonsense.com/wp-content/uploads/2023/10/MyGO-Meisekiha.jpg";
//        String result = getImagePageUrl(imageUrl);
//        System.out.println("搜索成功，结果 URL：" + result);
//    }

    public static void main(String[] args) {
        // 请求目标 URL
        String url = "https://graph.baidu.com/s?card_key=&entrance=GENERAL&extUiData%5BisLogoShow%5D=1&f=all&isLogoShow=1&session_id=14010902061855660855&sign=12637de4cde9c98a568ba01740101209&tpl_from=pc";
        String imageFirstUrl = getImageFirstUrl(url);
        System.out.println("搜索成功，结果 URL：" + imageFirstUrl);
    }

}
