package com.belladati.sdk.impl;

import com.belladati.httpclientandroidlib.HttpEntity;
import com.belladati.httpclientandroidlib.client.config.RequestConfig;
import com.belladati.httpclientandroidlib.client.methods.CloseableHttpResponse;
import com.belladati.httpclientandroidlib.client.methods.HttpPost;
import com.belladati.httpclientandroidlib.config.RegistryBuilder;
import com.belladati.httpclientandroidlib.conn.socket.ConnectionSocketFactory;
import com.belladati.httpclientandroidlib.conn.socket.PlainConnectionSocketFactory;
import com.belladati.httpclientandroidlib.entity.ByteArrayEntity;
import com.belladati.httpclientandroidlib.impl.client.CloseableHttpClient;
import com.belladati.httpclientandroidlib.impl.client.cache.CacheConfig;
import com.belladati.httpclientandroidlib.impl.client.cache.CachingHttpClientBuilder;
import com.belladati.httpclientandroidlib.impl.conn.PoolingHttpClientConnectionManager;
import com.belladati.sdk.BellaDatiService;
import com.belladati.sdk.dashboard.Dashboard;
import com.belladati.sdk.dashboard.DashboardInfo;
import com.belladati.sdk.dataset.AttributeValue;
import com.belladati.sdk.dataset.DataSet;
import com.belladati.sdk.dataset.DataSetInfo;
import com.belladati.sdk.dataset.data.DataTable;
import com.belladati.sdk.dataset.source.DataSource;
import com.belladati.sdk.dataset.source.DataSourceImport;
import com.belladati.sdk.dataset.source.DataSourcePendingImport;
import com.belladati.sdk.exception.dataset.data.UnknownServerColumnException;
import com.belladati.sdk.exception.server.NotFoundException;
import com.belladati.sdk.exception.server.UnexpectedResponseException;
import com.belladati.sdk.filter.Filter;
import com.belladati.sdk.report.Comment;
import com.belladati.sdk.report.Report;
import com.belladati.sdk.report.ReportInfo;
import com.belladati.sdk.user.User;
import com.belladati.sdk.util.CachedList;
import com.belladati.sdk.util.PaginatedIdList;
import com.belladati.sdk.util.PaginatedList;
import com.belladati.sdk.view.ViewLoader;
import com.belladati.sdk.view.ViewType;
import com.fasterxml.jackson.databind.JsonNode;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * Created by KarimT on 22.09.2016.
 */
public class BellaDatiServiceWrapper implements BellaDatiService {

    private CloseableHttpClient client;
    private BellaDatiServiceImpl service;

    public BellaDatiServiceWrapper(BellaDatiService service) {
        this.service = (BellaDatiServiceImpl) service;
    }

    public JsonNode loadJson(String uri) {
        return service.loadJson(uri);
    }

    @Override
    public PaginatedIdList<DashboardInfo> getDashboardInfo() {
        return service.getDashboardInfo();
    }

    @Override
    public Dashboard loadDashboard(String id) throws NotFoundException {
        return service.loadDashboard(id);
    }

    @Override
    public Object loadDashboardThumbnail(String id) throws IOException, NotFoundException {
        return service.loadDashboardThumbnail(id);
    }

    @Override
    public PaginatedIdList<ReportInfo> getReportInfo() {
        return service.getReportInfo();
    }

    @Override
    public Report loadReport(String id) throws NotFoundException {
        return service.loadReport(id);
    }

    @Override
    public Object loadReportThumbnail(String id) throws IOException, NotFoundException {
        return service.loadReportThumbnail(id);
    }

    @Override
    public PaginatedList<Comment> getReportComments(String reportId) throws NotFoundException {
        return service.getReportComments(reportId);
    }

    @Override
    public void postComment(String reportId, String text) throws NotFoundException {

    }

    @Override
    public Object loadViewContent(String viewId, ViewType viewType, Filter<?>... filters) throws NotFoundException {
        return service.loadViewContent(viewId, viewType, filters);
    }

    @Override
    public Object loadViewContent(String viewId, ViewType viewType, Collection<Filter<?>> filters) throws NotFoundException {
        return service.loadViewContent(viewId, viewType, filters);
    }

    @Override
    public ViewLoader createViewLoader(String viewId, ViewType viewType) {
        return service.createViewLoader(viewId, viewType);
    }

    @Override
    public CachedList<AttributeValue> getAttributeValues(String reportId, String attributeCode) throws NotFoundException {
        return service.getAttributeValues(reportId, attributeCode);
    }

    @Override
    public User loadUser(String userId) throws NotFoundException {
        return service.loadUser(userId);
    }

    @Override
    public Object loadUserImage(String userId) throws IOException, NotFoundException {
        return service.loadUserImage(userId);
    }

    @Override
    public PaginatedIdList<DataSetInfo> getDataSetInfo() {
        return service.getDataSetInfo();
    }

    @Override
    public DataSet loadDataSet(String id) throws NotFoundException {
        return service.loadDataSet(id);
    }

    @Override
    public void uploadData(String id, DataTable data) throws UnknownServerColumnException {

    }

    @Override
    public CachedList<DataSource> getDataSources(String id) throws NotFoundException {
        return service.getDataSources(id);
    }

    @Override
    public CachedList<DataSourceImport> getDataSourceImports(String id) throws NotFoundException {
        return service.getDataSourceImports(id);
    }

    @Override
    public DataSourcePendingImport setupDataSourceImport(String id, Date date) {
        return service.setupDataSourceImport(id, date);
    }

    @Override
    public byte[] post(String uri) throws URISyntaxException {
        return service.post(uri);
    }

    @Override
    public byte[] post(String uri, Map<String, String> uriParameters) throws URISyntaxException {
        return service.post(uri, uriParameters);
    }

    @Override
    public byte[] post(String uri, byte[] content) throws URISyntaxException {
        return service.post(uri, content);
    }

    @Override
    public byte[] post(String uri, Map<String, String> uriParameters, byte[] content) throws URISyntaxException {
        return service.post(uri, uriParameters, content);
    }

    @Override
    public byte[] postForm(String uri, Map<String, String> formParameters) throws URISyntaxException {
        return service.postForm(uri, formParameters);
    }

    @Override
    public byte[] postForm(String uri, Map<String, String> uriParameters, Map<String, String> formParameters) throws URISyntaxException {
        return service.postForm(uri, uriParameters, formParameters);
    }

    @Override
    public byte[] get(String uri) throws URISyntaxException {
        return service.get(uri);
    }

    @Override
    public byte[] get(String uri, Map<String, String> uriParameters) throws URISyntaxException {
        return service.get(uri, uriParameters);
    }
}

