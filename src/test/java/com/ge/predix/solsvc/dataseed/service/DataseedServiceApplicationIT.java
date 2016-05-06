package com.ge.predix.solsvc.dataseed.service;

import java.io.File;
import java.io.FileInputStream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.ge.predix.solsvc.dataseed.boot.DataseedServiceApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = DataseedServiceApplication.class)
@IntegrationTest({ "server.port=0" })
@ComponentScan("com.ge.predix.solsvc")
@WebAppConfiguration
@ActiveProfiles("local")
public class DataseedServiceApplicationIT {
    @Autowired
    private DataSeedService dataSeedService;

    @Test
    public void testLoadJson() throws Exception {
        File file = new File("src/main/resources/rmdapp/AssetData.json");
        FileInputStream stream = new FileInputStream(file);
        dataSeedService.uploadJsonData(file.getName(), stream);
    }

    @Test
    public void testLoadXls() throws Exception {
        File file = new File("src/main/resources/rmdapp/AssetData.xls");
        FileInputStream stream = new FileInputStream(file);
        dataSeedService.uploadXlsData(file.getName(), stream);
    }

    @Test
    @Ignore
    public void testLoadXml() throws Exception {
        File file = new File("src/main/resources/rmdapp/AssetData.xml");
        FileInputStream stream = new FileInputStream(file);
        dataSeedService.uploadXmlData(file.getName(), stream);
    }
}
