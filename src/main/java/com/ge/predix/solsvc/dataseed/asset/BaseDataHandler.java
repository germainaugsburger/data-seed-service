package com.ge.predix.solsvc.dataseed.asset;

import org.springframework.beans.factory.annotation.Autowired;

import com.ge.predix.solsvc.bootstrap.ams.common.AssetRestConfig;
import com.ge.predix.solsvc.restclient.impl.RestClient;

/**
 *
 * @author 212421693
 *
 */
public abstract class BaseDataHandler {

    @Autowired
    protected RestClient restClient;

    @Autowired
    protected AssetRestConfig assetRestConfig;
}
