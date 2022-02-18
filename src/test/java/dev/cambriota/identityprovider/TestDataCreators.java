package dev.cambriota.identityprovider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.cambriota.identityprovider.model.Subject;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class TestDataCreators {

    public static JsonNode getResourceAsJsonNode(String fileName) {
        try {
            File file = new File(TestDataCreators.class.getClassLoader().getResource(fileName).getFile());
            String data = FileUtils.readFileToString(file, "UTF-8");
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readTree(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Subject createTestSubject() {
        return new Subject(
                "my.name@example.org",
                "johndoe",
                "John",
                "Doe",
                "",
                LocalDateTime.now()
        );
    }

    public static String createTestDid() {
        return "did:iota:kv2fTV5BYBSpAMwvFN3b1z8iqcTk7ZHNa9AMeGY6pP6";
    }
}
