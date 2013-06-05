package uk.ac.susx.tag.filters;

import javax.servlet.*;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A servlet filter which sets the request and response character encodings.
 *
 * @author Hamish Morgan
 * @version 1.0 (05/06/2013)
 */
public class SetCharacterEncodingFilter implements Filter {

    private static final Logger LOG = Logger.getLogger(SetCharacterEncodingFilter.class.getName());
    //
    private static final String REQUEST_ENCODING_KEY = "requestEncoding";
    private static final String RESPONSE_ENCODING_KEY = "responseEncoding";
    private static final String FORCE_KEY = "force";
    //
    boolean force = false;
    String requestEncoding = null;
    String responseEncoding = null;

    public boolean isForce() {
        return force;
    }

    public void setForce(boolean force) {
        this.force = force;
    }

    public String getRequestEncoding() {
        return requestEncoding;
    }

    public void setRequestEncoding(String requestEncoding) throws UnsupportedCharsetException {
        if (requestEncoding != null)
            Charset.forName(requestEncoding);
        this.requestEncoding = requestEncoding;
    }

    public String getResponseEncoding() {
        return responseEncoding;
    }

    public void setResponseEncoding(String responseEncoding) throws UnsupportedCharsetException {
        if (responseEncoding != null)
            Charset.forName(responseEncoding);
        this.responseEncoding = responseEncoding;
    }

    public void init(FilterConfig filterConfig) throws ServletException {

        {
            Enumeration en = filterConfig.getInitParameterNames();
            while (en.hasMoreElements()) {
                Object name = en.nextElement();
                if (!(name.equals(REQUEST_ENCODING_KEY) || name.equals(RESPONSE_ENCODING_KEY) || name.equals(FORCE_KEY)))
                    LOG.log(Level.WARNING, "Unknown filter parameter: {0}", name);
            }
        }
        try {

            if (null != filterConfig.getInitParameter(FORCE_KEY)
                    && !filterConfig.getInitParameter(FORCE_KEY).trim().isEmpty())
                setForce(Boolean.valueOf(filterConfig.getInitParameter(FORCE_KEY)));

            if (null != filterConfig.getInitParameter(REQUEST_ENCODING_KEY)
                    && !filterConfig.getInitParameter(REQUEST_ENCODING_KEY).trim().isEmpty())
                setRequestEncoding(filterConfig.getInitParameter(REQUEST_ENCODING_KEY));

            if (null != filterConfig.getInitParameter(RESPONSE_ENCODING_KEY)
                    && !filterConfig.getInitParameter(RESPONSE_ENCODING_KEY).trim().isEmpty())
                setResponseEncoding(filterConfig.getInitParameter(RESPONSE_ENCODING_KEY));

            LOG.log(Level.INFO, "Set encodings: request={0}, response={1}, force",
                    new Object[]{
                            requestEncoding == null ? "Default" : requestEncoding,
                            responseEncoding == null ? "Default" : responseEncoding,
                            force});

        } catch (UnsupportedCharsetException e) {
            throw new ServletException("Filter could not be initialized; character encoding unsupported: ", e);
        }
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Conditionally select and set the request character encoding to be used
        if (requestEncoding != null && (force || (request.getCharacterEncoding() == null)))
            request.setCharacterEncoding(requestEncoding);

        // Conditionally select and set the response character encoding to be used
        if (responseEncoding != null && (force || (response.getCharacterEncoding() == null)))
            response.setCharacterEncoding(this.responseEncoding);

        chain.doFilter(request, response);
    }

    public void destroy() {
        // not a sausage
    }

    @Override
    public String toString() {
        return "SetCharacterEncodingFilter{" +
                " force=" + force +
                ", requestEncoding='" + requestEncoding + '\'' +
                ", responseEncoding='" + responseEncoding + '\'' +
                '}';
    }
}