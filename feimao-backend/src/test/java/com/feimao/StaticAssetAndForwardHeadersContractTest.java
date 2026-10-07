package com.feimao;

import org.junit.jupiter.api.Test;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticAssetAndForwardHeadersContractTest {

    @Test
    void legacyStaticAssetsArePackagedUnchanged() throws Exception {
        assertEquals(
                "3425143c30078df6fb7adeaedbf751be0ad22901cb1e75c0b75dd8819f05eeec",
                sha256("static/assets/img/avatar.png")
        );
        assertEquals(
                "f99a37a0a7818125993d4a8ea19ff457de1cacfd18a5c09e93ddacb3afa9b6e6",
                sha256("static/assets/img/qrcode.png")
        );
    }

    @Test
    void forwardedHeadersAreHandledBySpringFramework() throws Exception {
        ClassPathResource applicationYaml = new ClassPathResource("application.yml");
        List<PropertySource<?>> propertySources =
                new YamlPropertySourceLoader().load("application", applicationYaml);

        Object strategy = null;
        for (PropertySource<?> propertySource : propertySources) {
            Object configured = propertySource.getProperty("server.forward-headers-strategy");
            if (configured != null) {
                strategy = configured;
                break;
            }
        }

        assertEquals("framework", strategy);
    }

    private String sha256(String classpathLocation) throws Exception {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        assertTrue(resource.exists(), classpathLocation + " must exist on the runtime classpath");

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        try (InputStream input = resource.getInputStream()) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
        }

        StringBuilder hash = new StringBuilder();
        for (byte value : digest.digest()) {
            hash.append(String.format("%02x", value & 0xff));
        }
        return hash.toString();
    }
}
