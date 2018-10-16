/*
 * Copyright (C) 2013-2017 NTT DATA Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.terasoluna.gfw.web.codelist;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.Assert;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.terasoluna.gfw.common.codelist.CodeList;

/**
 * Interceptor class for setting codelist in attribute of {@link HttpServletRequest}
 * <p>
 * Default behavior is to set all the implementation beans of {@code CodeList} in the attribute of {@link HttpServletRequest}<br>
 * In order to narrow down the target beans, pass the pattern (regular expression) corresponding to codelist ID of target beans
 * <br>
 * to {@link #setCodeListIdPattern(Pattern)} method.
 * </p>
 */
public class CodeListInterceptor extends HandlerInterceptorAdapter implements
                                 ApplicationContextAware, InitializingBean {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(
            CodeListInterceptor.class);

    /**
     * list of {@link CodeList}
     */
    private Collection<CodeList> codeLists;

    /**
     * application context
     */
    private ApplicationContext applicationContext;

    /**
     * Pattern of Codelist IDs (Bean IDs) of codelists which are target to be set to attribute of {@link HttpServletRequest}.
     */
    private Pattern codeListIdPattern;

    /**
     * Sets codelist to the attribute of {@link HttpServletRequest}
     * <p>
     * Sets codelist to the attribute of {@link HttpServletRequest} before the execution of Controller.
     * </p>
     * @see org.springframework.web.servlet.handler.HandlerInterceptorAdapter#preHandle(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse, java.lang.Object)
     * @since 5.0.1
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception {

        if (codeLists == null) {
            return true;
        }

        for (CodeList codeList : codeLists) {
            String attributeName = codeList.getCodeListId();
            request.setAttribute(attributeName, codeList.asMap());
        }
        return true;
    }

    /**
     * Extracts the {@code CodeList}s which are to be set to the attribute of {@link HttpServletRequest}
     * <p>
     * Among the Beans which implement {@code CodeList} interface, extract the Codelist IDs(Bean IDs) which match<br>
     * with the regular expression specified in {@link #codeListIdPattern}.
     * </p>
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {

        Assert.notNull(applicationContext, "applicationContext is null.");

        if (this.codeListIdPattern == null) {
            this.codeListIdPattern = Pattern.compile(".+");
        }

        Map<String, CodeList> definedCodeLists = BeanFactoryUtils
                .beansOfTypeIncludingAncestors(applicationContext,
                        CodeList.class, false, false);
        Map<String, CodeList> targetCodeLists = new HashMap<String, CodeList>();
        for (CodeList codeList : definedCodeLists.values()) {
            String codeListId = codeList.getCodeListId();
            if (codeListId != null) {
                Matcher codeListIdMatcher = this.codeListIdPattern.matcher(
                        codeListId);
                if (codeListIdMatcher.matches()) {
                    targetCodeLists.put(codeListId, codeList);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("registered codeList : {}", targetCodeLists.keySet());
        }

        this.codeLists = Collections.unmodifiableCollection(targetCodeLists
                .values());

    }

    /**
     * Set the ApplicationContext.
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * Sets Pattern (regular expression) of Codelist IDs (Bean IDs) of codelists which are target to be set to attribute of
     * {@link HttpServletRequest}.
     * <p>
     * Default behavior is to include all beans
     * </p>
     * @param codeListIdPattern Pattern
     */
    public void setCodeListIdPattern(Pattern codeListIdPattern) {
        this.codeListIdPattern = codeListIdPattern;
    }

    /**
     * Returns the list of codelists which are to be set to attribute of {@link HttpServletRequest}
     * @return list of codelists
     */
    protected Collection<CodeList> getCodeLists() {
        return codeLists;
    }

}
