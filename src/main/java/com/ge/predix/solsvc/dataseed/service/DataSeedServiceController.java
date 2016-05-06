package com.ge.predix.solsvc.dataseed.service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.zip.DataFormatException;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2AccessDeniedException;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordResourceDetails;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.multipart.MultipartFile;

import com.ge.predix.solsvc.restclient.config.IOauthRestConfig;

@RestController
public class DataSeedServiceController {
    private static final Logger logger = LoggerFactory.getLogger(DataSeedServiceController.class);

    @Autowired
    private IOauthRestConfig restConfig;

    @Autowired
    private HttpServletRequest context;

    @Autowired
    private DataSeedService dataSeedService;

    @Value("${acsSubZone:#{null}}")
    private String acsSubZone;

    @SuppressWarnings("nls")
    private String uploadAssetData(MultipartFile file) {
        String name = file.getName();
        if (file.isEmpty()) {
            return "You failed to upload " + name //$NON-NLS-1$
                + " because the file was empty."; //$NON-NLS-1$
        }
        try {
            if (StringUtils.isNotEmpty(file.getOriginalFilename())
                && file.getOriginalFilename().toLowerCase().endsWith("xls")) {

                return dataSeedService.uploadXlsData(name, file.getInputStream());
            } else if (StringUtils.isNotEmpty(file.getOriginalFilename())
                && file.getOriginalFilename().toLowerCase().endsWith("xml")) {

                return dataSeedService.uploadXmlData(name, file.getInputStream());
            }
            // otherwise its a json file
            return dataSeedService.uploadJsonData(name, file.getInputStream());
        } catch (IOException e) {
            String errorMsg =
                "You failed to upload file" + name + " due to IOException => " //$NON-NLS-1$ //$NON-NLS-2$
                    + e.getMessage();
            logger.error(errorMsg, e);
            return errorMsg;
        } catch (DataFormatException e) {
            String errorMsg =
                "You failed to upload file " + name + " due to DataFormatException=> " //$NON-NLS-1$ //$NON-NLS-2$
                    + e.getMessage();
            logger.error(errorMsg, e);
            return errorMsg;
        }
    }

    /**
     * The method is called from the index.html to upload the Asset.xls
     * spreadsheet to import data into Asset . This api redirects to
     * validateUser endpoint with username and password. This validateUser
     * endpoint is protected by ACS using acs-security-extension using spring
     * security. The configuration for the acs-security-extension using
     * spring-security is located in the
     * src/main/resources/META-INF/spring/dataseed-service-acs-context.xml . The
     * spring security extension , based on the username , password and setting
     * on the VCAPS or application.properties , calls the OAuth provider to get
     * the token and check on the ACS to evaluate the policy.Once the policy is
     * evaluated to PERMIT , then the call is pass forward to the get the token
     * based on client credentials and call asset endpoints. Spring Security
     * acs-extension if the policy evaluated is condition is resolved to DENY ,
     * this then raises an OAuth2AccessDeniedException and the same exception is
     * reported .
     */
    @SuppressWarnings("nls")
    @RequestMapping(value = "/uploadAssetData", method = RequestMethod.POST)
    public
    @ResponseBody
    String uploadAssetData(
        @RequestParam(value = "username", required = true) String username,
        @RequestParam(value = "password", required = true) String password,
        @RequestParam(value = "file", required = true) MultipartFile file) {

        // 1. Create a OAuthRestTemplate to call the validateUser endpoint.
        OAuth2RestTemplate restTemplate = getRestTemplate(username, password);

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        String dataSeedUrl = this.context.getRequestURL().toString().replace("/uploadAssetData", "/validateuser");

        dataSeedUrl = dataSeedUrl.replaceAll("http","https"); // this is required since all traffic will be https

        logger.info("XXXCalling data seed URL " + dataSeedUrl);

        // 2 . ValidateUser endpoint is protected by ACS using
        // acs-security-extension using spring security.
        // The configuration for the acs-security-extension using
        // spring-security is located in the
        // src/main/resources/META-INF/spring/dataseed-service-acs-context.xml .
        // The spring security extension , based on the username , password and
        // setting on the VCAPS or application.properties , calls the OAuth
        // provider to get the token and check on the
        // ACS to evaluate the policy.Once the policy is evaluated to PERMIT ,
        // then the call is pass forward to the get the token based on client
        // credentials and call asset endpoints.
        try {
            // 3. Once the policy is evaluated to PERMIT , then the call is pass
            // forward to the get the token based on client credentials and call
            // asset endpoints.
            restTemplate.postForObject(new URI(dataSeedUrl), new HttpEntity<Object>(map), String.class);

            return uploadAssetData(file);
        }
        // 4 .If the policy evaluated is condition is resolved to DENY , this
        // then raises an OAuth2AccessDeniedException and the same exception is
        // returned back as response.
        catch (OAuth2AccessDeniedException e) {
            logger.error("Error validating user " + username
                + " with following error " + e.getCause() + e.getMessage()
                + e);
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            logger.error("Error uploading to Asset for user " + username
                + " with following error " + e.getCause() + e.getMessage()
                + e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a OAuth2RestTemplate based on the username password
     */
    @SuppressWarnings("nls")
    private OAuth2RestTemplate getRestTemplate(String username, String password) {
        // get token here based on username password;
        ResourceOwnerPasswordResourceDetails resourceDetails = new ResourceOwnerPasswordResourceDetails();
        resourceDetails.setUsername(username);
        resourceDetails.setPassword(password);

        String url = this.restConfig.getOauthResourceProtocol() + "://"
            + this.restConfig.getOauthRestHost()
            + this.restConfig.getOauthResource();

        resourceDetails.setAccessTokenUri(url);

        String[] clientIds = this.restConfig.getOauthClientId().split(":");
        resourceDetails.setClientId(clientIds[0]);
        resourceDetails.setClientSecret(clientIds[1]);

        return new OAuth2RestTemplate(resourceDetails);
    }

    /**
     * Endpoint is gated with ACS and validates the policy for condition by
     * spring security Interceptors and filters. The configuration for the
     * acs-security-extension using spring-security is located in the
     * src/main/resources/META-INF/spring/dataseed-service-acs-context.xml. If
     * the policy is evaluated to success , the call proceeds to generate token
     * based on client_credentials and returns this token back to the caller .
     */
    @SuppressWarnings("nls")
    @RequestMapping(value = "/validateuser", method = RequestMethod.POST)
    public String validateUser() throws Exception {

        // Get token based on the client_credentials to access Asset and
        // time series
        logger.info("getting token based on the client_credentials");
        try {
            ClientCredentialsResourceDetails resourceDetails = new ClientCredentialsResourceDetails();
            String url = this.restConfig.getOauthResourceProtocol() + "://"
                + this.restConfig.getOauthRestHost()
                + this.restConfig.getOauthResource();
            resourceDetails.setAccessTokenUri(url);

            String[] clientIds = this.restConfig.getOauthClientId().split(":");
            resourceDetails.setClientId(clientIds[0]);
            resourceDetails.setClientSecret(clientIds[1]);

            OAuth2RestTemplate restTemplate = new OAuth2RestTemplate(resourceDetails);
            OAuth2AccessToken token = restTemplate.getAccessToken();

            return token.getTokenType() + " " + token.getValue();
        } catch (HttpClientErrorException hce) {
            throw new Exception(hce);
        }
    }
}
