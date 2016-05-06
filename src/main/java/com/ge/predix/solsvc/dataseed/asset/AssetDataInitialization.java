package com.ge.predix.solsvc.dataseed.asset;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.DataFormatException;

import javax.annotation.PostConstruct;

import org.apache.http.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ge.predix.solsvc.bootstrap.ams.dto.Asset;
import com.ge.predix.solsvc.bootstrap.ams.dto.AssetMeter;
import com.ge.predix.solsvc.bootstrap.ams.dto.Attribute;
import com.ge.predix.solsvc.bootstrap.ams.dto.MeterDatasource;
import com.ge.predix.solsvc.bootstrap.ams.factories.AssetFactory;
import com.ge.predix.solsvc.bootstrap.ams.factories.ClassificationFactoryImpl;
import com.ge.predix.solsvc.ext.util.JsonMapper;


/**
 * 
 * @author predix
 */
@Component
@SuppressWarnings({ "nls", "unused" })
public class AssetDataInitialization extends AttributableObjectDataHandler {
    private static final Logger log = LoggerFactory.getLogger(AssetDataInitialization.class);

    private static final int ASSET_URI = 0;
    private static final int ASSET_NAME = 1;
    private static final int ASSET_DESC = 2;
    private static final int ASSET_CLASSIFICATION_URI = 3;
    private static final int ASSET_PARENT_URI = 4;

    private static final int ASSET_ATTRIBUTE_NAME = 5;
    private static final int ASSET_ATTRIBUTE_VALUE = 6;
    private static final int ASSET_ATTRIBUTE_CARDINALITY = 7;
    private static final int ASSET_ATTRIBUTE_TYPE = 8;
    private static final int ASSET_ATTRIBUTE_REQUIRED = 9;
    private static final int ASSET_ATTRIBUTE_UNIQUE_FLAG = 10;
    private static final int ASSET_ATTRIBUTE_DISPLAY_FLAG = 11;
    private static final int ASSET_ATTRIBUTE_ENTROPY = 12;
    private static final int ASSET_ATTRIBUTE_ENUMERATION = 13;
    private static final int ASSET_ATTRIBUTE_UOM = 14;

    private static final int ASSET_SIBLING_SORT_ORDER = 15;
    private static final int ASSET_GROUP_URI = 16;
    private static final int ASSET_STATE_URI = 17;
    private static final int ASSET_TEMPLATE_URI = 18;
    private static final int ASSET_TEMPLATE_POSITION = 19;
    private static final int ASSET_EMPTY_FLAG = 20;
    private static final int ASSET_OBSOLETE_FLAG = 21;
    private static final int ASSET_NON_SERIALIZED_QUANTITIES = 22;
    private static final int ASSET_TAGS = 23;
    private static final int ASSET_PROPAGATE_STATE = 24;

    private static final int METER_URI = 25;
    private static final int METER_NAME = 26;
    private static final int METER_DESC = 27;
    private static final int METER_UOM = 28;
    private static final int METER_TYPE = 29;
    private static final int METER_DATA_TYPE = 30;
    private static final int METER_TAGS = 31;
    private static final int METER_DATASOURCE_NODE_NAME = 32;
    private static final int METER_DATASOURCE_IS_KPI = 33;
    private static final int METER_DATASOURCE_FIELD_URI = 34;
    private static final int METER_DATASOURCE_CONTROLLER_URI = 35;
    private static final int METER_DATASOURCE_MACHINE_URI = 36;
    private static final int METER_DATASOURCE_METER_EXTENSIONS_URI = 37;
    private static final int METER_IS_MANUAL_FLAG = 38;
    private static final int METER_SOURCE_TAG_ID = 39;
    private static final int METER_OUTPUT_MIN = 40;
    private static final int METER_OUTPUT_MAX = 41;
    
    @Autowired
    private AssetFactory                assetFactory;
    @Autowired
    private ClassificationFactoryImpl classification;
    @Autowired
    private JsonMapper jsonMapper;

    /**
     * 
     */
    @PostConstruct
    public void intilizeAssetData()
    {
        log.debug("*******************Seed data Initialization complete*********************");
    }

    /**
     * @param content -
     * @param headers -
     * @param authorization -
     * @param appId -
     * @throws DataFormatException -
     */

    public void seedData(Map<String, String[][]> content, List<Header> headers) throws DataFormatException {
        String[][] assets = content.get("Asset");

        Asset asset = null;
        Attribute attribute = null;
        LinkedHashMap<String, Attribute> attributeMap = null;
        LinkedHashMap<String, AssetMeter> meterMap = null;
        for (String[] row : assets) {
            if (asset == null || (row[ASSET_URI] != null && !row[ASSET_URI].isEmpty() && !asset.getUri()
                .equals(row[ASSET_URI]))) {
                if (asset != null) {
                    this.assetFactory.createAsset(asset, headers);
                }
                //if the assetURI is changed then make call to create the existing asset
                if (asset != null && !asset.getUri().equals(row[ASSET_URI])) {
                    this.assetFactory.createAsset(asset, headers);
                }
                asset = new Asset();
                asset.setUri(row[ASSET_URI]);
                asset.setAssetId(row[ASSET_NAME]);
                asset.setDescription(row[ASSET_DESC]);
                asset.setGroup(row[ASSET_GROUP_URI]);
                asset.setSpecification(row[ASSET_CLASSIFICATION_URI]);
                asset.setParent(row[ASSET_PARENT_URI]);

                /*************ATTRIBUTE***************************/
                attributeMap = new LinkedHashMap<>();
                attribute = putAttributeInMap(attribute, attributeMap, row);
                if (!attributeMap.isEmpty()) {
                    asset.setAttributes(attributeMap);
                }

                /**************ASSET METER*************************/
                meterMap = new LinkedHashMap<>();
                putMeterInMap(meterMap, row);
                if (!meterMap.isEmpty()) {
                    asset.setAssetMeter(meterMap);
                }
            } else {
                attribute = putAttributeInMap(attribute, attributeMap, row);
                putMeterInMap(meterMap, row);
            }
        }
        //if asset is not null and the last row is reached
        if (asset != null) {
            this.assetFactory.createAsset(asset, headers);
        }
    }

    @Override
    protected int getAttributeNameIndex() {
        return ASSET_ATTRIBUTE_NAME;
    }

    @Override
    protected int getAttributeValueIndex() {
        return ASSET_ATTRIBUTE_VALUE;
    }

    @Override
    protected int getAttributeEnumerationIndex() {
        return ASSET_ATTRIBUTE_ENUMERATION;
    }

    @Override
    protected int getAttributeTypeIndex() {
        return ASSET_ATTRIBUTE_TYPE;
    }

    @Override
    protected int getAttributeUomIndex() {
        return ASSET_ATTRIBUTE_UOM;
    }

    private void putMeterInMap(LinkedHashMap<String, AssetMeter> meterMap,
                               String[] row) {
        if (row[METER_NAME] == null || row[METER_NAME].isEmpty()) {
            return;
        }
        AssetMeter meter = new AssetMeter();
        meter.setUri(row[METER_URI]);
        MeterDatasource mds = new MeterDatasource();
        mds.setNodeName(row[METER_DATASOURCE_NODE_NAME]);
        mds.setIsKpi(row[METER_DATASOURCE_IS_KPI]);
        mds.setFieldUri(row[METER_DATASOURCE_FIELD_URI]);
        mds.setControllerUri(row[METER_DATASOURCE_CONTROLLER_URI]);
        mds.setMachineUri(row[METER_DATASOURCE_MACHINE_URI]);
        mds.setMeterExtensionsUri(row[METER_DATASOURCE_METER_EXTENSIONS_URI]);
        meter.setMeterDatasource(mds);
        meter.setIsManual(Boolean.valueOf(row[METER_IS_MANUAL_FLAG]));
        meter.setSourceTagId(row[METER_SOURCE_TAG_ID]);
        if (row[METER_OUTPUT_MAX] != null) {
            meter.setOutputMaximum(new Double(row[METER_OUTPUT_MAX]));
        }
        if (row[METER_OUTPUT_MIN] != null) {
            meter.setOutputMinimum(new Double(row[METER_OUTPUT_MIN]));
        }
        meterMap.put(row[METER_NAME], meter);
    }
}