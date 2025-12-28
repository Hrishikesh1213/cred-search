package com.cred.search.models.response.wrapper;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ApiResponse<T> {
    @JsonProperty("status")
    private ResponseStatus status;
    @JsonProperty("response_message")
    private String responseMessage;
    @JsonProperty("response_code")
    private String responseCode;
    @JsonProperty("data")
    private T data;

    public ApiResponse(ResponseStatus status, String responseMessage, String responseCode, T data) {
        this.status = status;
        this.responseMessage = responseMessage;
        this.responseCode = responseCode;
        this.data = data;
    }

    public ApiResponse() {
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<T>(ResponseStatus.SUCCESS, ResponseStatus.SUCCESS.toString(), ResponseStatus.SUCCESS.toString(), data);
    }

    public static <T, E extends Enum<E>> ApiResponse<T> success(E responseCode, T data) {
        return new ApiResponse<T>(ResponseStatus.SUCCESS, responseCode.toString(), responseCode.toString(), data);
    }

    public static <T, E extends Enum<E>> ApiResponse<T> success(String successMessage, E responseCode, T data) {
        return new ApiResponse<T>(ResponseStatus.SUCCESS, successMessage, responseCode.toString(), data);
    }

    public static <T, E extends Enum<E>> ApiResponse<T> failure(String failureMessage, E responseCode, T data) {
        return new ApiResponse<T>(ResponseStatus.FAILURE, failureMessage, responseCode.toString(), data);
    }

    public static <T> ApiResponseBuilder<T> builder() {
        return new ApiResponseBuilder<T>();
    }

    public ResponseStatus getStatus() {
        return this.status;
    }

    @JsonProperty("status")
    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public String getResponseMessage() {
        return this.responseMessage;
    }

    @JsonProperty("response_message")
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public String getResponseCode() {
        return this.responseCode;
    }

    @JsonProperty("response_code")
    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public T getData() {
        return this.data;
    }

    @JsonProperty("data")
    public void setData(T data) {
        this.data = data;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof ApiResponse)) {
            return false;
        } else {
            ApiResponse<?> other = (ApiResponse) o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$status = this.getStatus();
                Object other$status = other.getStatus();
                if (this$status == null) {
                    if (other$status != null) {
                        return false;
                    }
                } else if (!this$status.equals(other$status)) {
                    return false;
                }

                Object this$responseMessage = this.getResponseMessage();
                Object other$responseMessage = other.getResponseMessage();
                if (this$responseMessage == null) {
                    if (other$responseMessage != null) {
                        return false;
                    }
                } else if (!this$responseMessage.equals(other$responseMessage)) {
                    return false;
                }

                Object this$responseCode = this.getResponseCode();
                Object other$responseCode = other.getResponseCode();
                if (this$responseCode == null) {
                    if (other$responseCode != null) {
                        return false;
                    }
                } else if (!this$responseCode.equals(other$responseCode)) {
                    return false;
                }

                Object this$data = this.getData();
                Object other$data = other.getData();
                if (this$data == null) {
                    if (other$data != null) {
                        return false;
                    }
                } else if (!this$data.equals(other$data)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof ApiResponse;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $status = this.getStatus();
        result = result * 59 + ($status == null ? 43 : $status.hashCode());
        Object $responseMessage = this.getResponseMessage();
        result = result * 59 + ($responseMessage == null ? 43 : $responseMessage.hashCode());
        Object $responseCode = this.getResponseCode();
        result = result * 59 + ($responseCode == null ? 43 : $responseCode.hashCode());
        Object $data = this.getData();
        result = result * 59 + ($data == null ? 43 : $data.hashCode());
        return result;
    }

    public String toString() {
        return "ApiResponse(status=" + this.getStatus() + ", responseMessage=" + this.getResponseMessage() + ", responseCode=" + this.getResponseCode() + ", data=" + this.getData() + ")";
    }

    public static class ApiResponseBuilder<T> {
        private ResponseStatus status;
        private String responseMessage;
        private String responseCode;
        private T data;

        ApiResponseBuilder() {
        }

        @JsonProperty("status")
        public ApiResponseBuilder<T> status(ResponseStatus status) {
            this.status = status;
            return this;
        }

        @JsonProperty("response_message")
        public ApiResponseBuilder<T> responseMessage(String responseMessage) {
            this.responseMessage = responseMessage;
            return this;
        }

        @JsonProperty("response_code")
        public ApiResponseBuilder<T> responseCode(String responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        @JsonProperty("data")
        public ApiResponseBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponse<T> build() {
            return new ApiResponse<T>(this.status, this.responseMessage, this.responseCode, this.data);
        }

        public String toString() {
            return "ApiResponse.ApiResponseBuilder(status=" + this.status + ", responseMessage=" + this.responseMessage + ", responseCode=" + this.responseCode + ", data=" + this.data + ")";
        }
    }
}
