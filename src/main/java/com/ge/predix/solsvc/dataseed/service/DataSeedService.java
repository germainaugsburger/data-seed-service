package com.ge.predix.solsvc.dataseed.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.sf.json.JSON;
import net.sf.json.xml.XMLSerializer;
import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.predix.solsvc.bootstrap.ams.common.AssetRestConfig;
import com.ge.predix.solsvc.bootstrap.ams.factories.ModelFactory;
import com.ge.predix.solsvc.dataseed.asset.AssetDataInitialization;
import com.ge.predix.solsvc.dataseed.asset.ClassificationDataInitialization;
import com.ge.predix.solsvc.dataseed.asset.GroupDataInitialization;
import com.ge.predix.solsvc.dataseed.asset.MeterDataInitialization;
import com.ge.predix.solsvc.dataseed.util.SpreadSheetParser;
import com.ge.predix.solsvc.restclient.impl.RestClient;

@Component
public class DataSeedService {
    private static final Logger logger = LoggerFactory.getLogger(DataSeedService.class);

    @Autowired
    private AssetDataInitialization assetDataInit;

    @Autowired
    private MeterDataInitialization meterDataInit;

    @Autowired
    private GroupDataInitialization groupDataInit;

    @Autowired
    private ClassificationDataInitialization classDataInit;

    @Autowired
    private RestClient restClient;

    @Autowired
    private ModelFactory modelFactory;

    @Autowired
    private AssetRestConfig assetRestConfig;

    private ObjectMapper mapper = new ObjectMapper();

    public String uploadXlsData(String name, InputStream data) throws DataFormatException {
        List<String> workSheets = new ArrayList<>();
        workSheets.add("Asset"); //$NON-NLS-1$
        workSheets.add("Fields"); //$NON-NLS-1$
        workSheets.add("Meter"); //$NON-NLS-1$
        workSheets.add("Classification"); //$NON-NLS-1$
        workSheets.add("Group"); //$NON-NLS-1$

        SpreadSheetParser parser = new SpreadSheetParser();
        Map<String, String[][]> content = parser.parseInputFile(data, workSheets);

        logger.debug("zoneId=" + this.assetRestConfig.getZoneId()); //$NON-NLS-1$
        List<Header> headers = this.restClient.getSecureTokenForClientId();
        this.restClient.addZoneToHeaders(headers, this.assetRestConfig.getZoneId());

        this.classDataInit.seedData(content, headers);
        this.groupDataInit.seedData(content, headers);
        this.meterDataInit.seedData(content, headers);
        this.assetDataInit.seedData(content, headers);

        return "You successfully uploaded " + name + "!"; //$NON-NLS-1$//$NON-NLS-2$
    }

    public String uploadXmlData(String name, InputStream data) throws IOException {
        XMLSerializer serializer = new XMLSerializer();
        JSON json = serializer.readFromStream(data);
        Map map = this.mapper.readValue(json.toString(), Map.class);
        uploadJson(map);

        return String.format("You successfully uploaded %s!", name);
    }

    public String uploadJsonData(String name, InputStream data) throws IOException {
        logger.debug("Processing Json data file " + name); //$NON-NLS-1$
        Map map = this.mapper.readValue(data, Map.class);
        uploadJson(map);

        return "You successfully uploaded " + name + "!"; //$NON-NLS-1$//$NON-NLS-2$;
    }

    private void uploadJson(Map map) {
        logger.debug("zoneId={}", this.assetRestConfig.getZoneId());
        List<Header> headers = this.restClient.getSecureTokenForClientId();
        this.restClient.addZoneToHeaders(headers, this.assetRestConfig.getZoneId());

        for (Object key : map.keySet()) {
            Object value = map.get(key);

            if (value instanceof List) {
                modelFactory.createModel((List) value, headers);
            } else {
                List<Object> list = new LinkedList<>();
                list.add(value);
                modelFactory.createModel(list, headers);
            }
        }
    }
}
