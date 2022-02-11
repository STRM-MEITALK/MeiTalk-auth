package com.meitalk.auth.config;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;

@Slf4j
public class JsonUtils {

    public JsonUtils() {
    }

    public static JSONObject readJSONStringFromRequestBody(HttpServletRequest request) {
        StringBuffer json = new StringBuffer();
        String line = null;

        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                json.append(line);
            }
        } catch (IOException e) {
            log.error("Error reading JSON String ===> readJSONStringFromRequestBody Class" + e.toString());
        }

        JSONObject jobj = new JSONObject(json.toString());
        return jobj;
    }

}
