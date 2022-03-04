package io.github.sinri.keel.test.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.github.sinri.keel.core.properties.KeelOptions;
import io.github.sinri.keel.test.helper.yaml.FirstLevelOptions;

import java.io.IOException;
import java.net.URL;

public class OptionsTest {
    public static void main(String[] args) {
        FirstLevelOptions firstLevelOptions = KeelOptions.loadWithYamlFilePath("test.yml", FirstLevelOptions.class);
        System.out.println(firstLevelOptions);

    }

    public static void test1() {
        FirstLevelOptions firstLevelOptions = KeelOptions.loadWithYamlFilePath("test.yml", FirstLevelOptions.class);
        System.out.println(firstLevelOptions);

//        Keel.initializeVertx(new VertxOptions());

//        Keel.getVertx().fileSystem()
//                .readFile("test.yml")
//                .recover(throwable -> {
//                    URL resource = KeelOptions.class.getClassLoader().getResource("test.yml");
//                    assert resource != null;
//                    return Keel.getVertx().fileSystem().readFile(resource.getPath());
//                })
//                .compose(buffer->{
//                    return new YamlProcessor().process(Keel.getVertx(),null,buffer);
//                })
//                .compose(jsonObject -> {
//                    System.out.println(jsonObject);
//                    return Future.succeededFuture();
//                });
    }

    public static void test2() {
        var mapper = new ObjectMapper(new YAMLFactory());
        mapper.findAndRegisterModules();

        URL resource = KeelOptions.class.getClassLoader().getResource("test.yml");
        FirstLevelOptions firstLevelOptions = null;
        try {
            firstLevelOptions = mapper.readValue(resource, FirstLevelOptions.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(firstLevelOptions);
    }
}
