package io.github.sinri.keel.test.v1.helper.yaml;

import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import io.github.sinri.keel.core.KeelHelper;

import java.io.IOException;

public class YAMLTest {
    public static void main(String[] args) {
        byte[] bytes = new byte[0];
        try {
            bytes = KeelHelper.readFileAsByteArray("test.yml", true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        YAMLFactory yamlFactory = new YAMLFactory();
        //var mapper = new ObjectMapper(yamlFactory);
        //mapper.findAndRegisterModules();

        try {
            YAMLParser parser = yamlFactory.createParser(bytes);
            while (parser.nextFieldName() != null) {

//            }
//            while (parser.nextToken() != null) {
                System.out.println(parser.currentToken());
                System.out.println("\t" + parser.currentName() + " = " + parser.currentValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


//        return mapper.readValue(new String(bytes, StandardCharsets.UTF_8), classOfT);
    }
}
