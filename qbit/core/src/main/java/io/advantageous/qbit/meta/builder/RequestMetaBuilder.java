package io.advantageous.qbit.meta.builder;

import io.advantageous.boon.core.Str;
import io.advantageous.boon.core.TypeType;
import io.advantageous.boon.core.reflection.AnnotationData;
import io.advantageous.boon.core.reflection.MethodAccess;
import io.advantageous.qbit.annotation.RequestMethod;
import io.advantageous.qbit.meta.CallType;
import io.advantageous.qbit.meta.ParameterMeta;
import io.advantageous.qbit.meta.RequestMeta;
import io.advantageous.qbit.meta.params.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Allows you to build request meta data.
 */
public class RequestMetaBuilder {

    private CallType callType;
    private String requestURI;
    private List<ParameterMeta> parameters = new ArrayList<>();
    private List<RequestMethod> requestMethods = new ArrayList<>();
    private  String description;


    public static RequestMetaBuilder requestMetaBuilder() {
        return new RequestMetaBuilder();
    }

    public static int findURIPosition(String path, String findString) {

        final String[] pathParts = Str.split(path, '/');
        int position;
        for (position = 0; position < pathParts.length; position++) {

            String pathPart = pathParts[position];
            if (pathPart.equals(findString)) {
                break;
            }
        }
        return position;
    }


    public String getDescription() {
        return description;
    }

    public RequestMetaBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public CallType getCallType() {
        return callType;

    }

    public RequestMetaBuilder setCallType(CallType callType) {
        this.callType = callType;
        return this;
    }

    public String getRequestURI() {
        return requestURI;
    }

    public RequestMetaBuilder setRequestURI(String requestURI) {
        this.requestURI = requestURI;
        return this;
    }

    public List<ParameterMeta> getParameters() {
        return parameters;
    }

    public RequestMetaBuilder setParameters(List<ParameterMeta> parameters) {
        this.parameters = parameters;
        return this;
    }

    public RequestMetaBuilder addParameter(ParameterMeta parameter) {
        this.parameters.add(parameter);
        return this;
    }

    public RequestMetaBuilder addParameters(ParameterMeta... parameters) {
        Collections.addAll(this.getParameters(), parameters);
        return this;
    }

    public List<RequestMethod> getRequestMethods() {
        return requestMethods;
    }

    public RequestMetaBuilder setRequestMethods(List<RequestMethod> requestMethods) {
        this.requestMethods = requestMethods;
        return this;
    }

    public RequestMeta build() {
        return new RequestMeta(getCallType(), getRequestMethods(),
                getRequestURI(), getParameters());
    }

    public void addParameters(final String rootPath, final String servicePath,
                              final String path, final MethodAccess methodAccess) {

        final List<List<AnnotationData>> paramsAnnotationData = methodAccess.annotationDataForParams();

        final List<TypeType> typeTypes = methodAccess.paramTypeEnumList();


        final List<ParameterMeta> params = new ArrayList<>(typeTypes.size());


        for (int index = 0; index < typeTypes.size(); index++) {

            if (paramsAnnotationData.size() > index) {

                final List<AnnotationData> annotationDataList = paramsAnnotationData.get(index);

                final String finalPath = Str.join("/", rootPath, servicePath, path).replace("//", "/");

                if (annotationDataList == null || annotationDataList.size() == 0) {
                    Param requestParam = getParam(finalPath, null, index);
                    final ParameterMeta param = createParamMeta(methodAccess, index, typeTypes, requestParam);


                    params.add(param);
                    continue;
                }

                for (AnnotationData annotationData : annotationDataList) {


                    Param requestParam = getParam(finalPath, annotationData, index);

                    if (requestParam != null) {
                        final ParameterMeta param = createParamMeta(methodAccess, index, typeTypes, requestParam);
                        params.add(param);
                        break;
                    }
                }
            }
        }

        this.parameters.addAll(params);


    }

    private ParameterMeta createParamMeta(final MethodAccess methodAccess, final int index,
                                          final List<TypeType> typeTypes, final Param requestParam) {

        ParameterMetaBuilder builder = ParameterMetaBuilder.parameterMetaBuilder();
        builder.setType(typeTypes.get(index));
        builder.setParam(requestParam);

        Type type = methodAccess.method().getGenericParameterTypes()[index];

        if (type instanceof ParameterizedType) {

            ParameterizedType parameterizedType = ((ParameterizedType) type);

            Class containerClass = (Class) parameterizedType.getRawType();
            builder.setClassType(containerClass);

            /* It is a collection or a map. */
            if (Collection.class.isAssignableFrom(containerClass)) {
                builder.setCollection(true);
                builder.setComponentClass((Class)parameterizedType.getActualTypeArguments()[0]);
            } else if (Map.class.isAssignableFrom(containerClass)){
                builder.setMap(true);
                builder.setComponentClassKey((Class) parameterizedType.getActualTypeArguments()[0]);
                builder.setComponentClassValue((Class) parameterizedType.getActualTypeArguments()[1]);
            }
        } else {
            Class classType = methodAccess.method().getParameterTypes()[index];
            builder.setClassType(classType);
            builder.setComponentClass(classType.getComponentType());
            builder.setArray(classType.isArray());
        }

        return builder.build();
    }

    private Param getParam(final String path, final AnnotationData annotationData, final int index) {

        if (annotationData == null) {
            return new BodyParam(true, null, null);
        }

        Param param;
        String paramName = getParamName(annotationData);

        boolean required = getRequired(annotationData);

        String description = getParamDescription(annotationData);


        String defaultValue = getDefaultValue(annotationData);

        switch (annotationData.getName()) {
            case "requestParam":
                param = new RequestParam(required, paramName, defaultValue, description);
                break;
            case "headerParam":
                param = new HeaderParam(required, paramName, defaultValue, description);
                break;
            case "pathVariable":

                if (!path.contains("{")) {
                    throw new IllegalStateException();
                }
                if (paramName == null || Str.isEmpty(paramName)) {

                    String findString = "{" + index + "}";

                    int position = findURIPosition(path, findString);

                    param = new URIPositionalParam(required, index, defaultValue, position, description);
                } else {
                    String findString = "{" + paramName + "}";
                    int position = findURIPosition(path, findString);
                    param = new URINamedParam(required, paramName, defaultValue, position, description);
                }
                break;
            default:
                param = null;
        }

        return param;
    }

    private String getDefaultValue(AnnotationData annotationData) {

        if (annotationData == null)
            return null;

        final Object value = annotationData.getValues().get("defaultValue");
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    private String getParamName(AnnotationData annotationData) {

        if (annotationData == null)
            return null;

        final Object value = annotationData.getValues().get("value");
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    private String getParamDescription(AnnotationData annotationData) {

        if (annotationData == null)
            return null;

        final Object value = annotationData.getValues().get("description");
        if (value == null) {
            return null;
        }

        return value.toString();
    }

    private Boolean getRequired(AnnotationData annotationData) {

        if (annotationData == null)
            return false;

        final Object value = annotationData.getValues().get("required");
        if (value == null) {
            return false;
        }

        return value instanceof Boolean ? ((Boolean) value) : Boolean.valueOf(value.toString());
    }
}
