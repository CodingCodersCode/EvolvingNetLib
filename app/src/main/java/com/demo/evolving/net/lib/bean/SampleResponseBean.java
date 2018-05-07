package com.demo.evolving.net.lib.bean;

/**
 * Date：2018/5/7 17:24
 * <p>
 * author: CodingCodersCode
 */
public class SampleResponseBean {

    private int accessToken;
    private String refreshToken;
    private String currentLoginDate;
    private UserBean user;
    private boolean bindFlag;
    private ProductsBean products;
    private int messageCount;

    public int getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(int accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getCurrentLoginDate() {
        return currentLoginDate;
    }

    public void setCurrentLoginDate(String currentLoginDate) {
        this.currentLoginDate = currentLoginDate;
    }

    public UserBean getUser() {
        return user;
    }

    public void setUser(UserBean user) {
        this.user = user;
    }

    public boolean isBindFlag() {
        return bindFlag;
    }

    public void setBindFlag(boolean bindFlag) {
        this.bindFlag = bindFlag;
    }

    public ProductsBean getProducts() {
        return products;
    }

    public void setProducts(ProductsBean products) {
        this.products = products;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }

    public static class UserBean {
        /**
         * userId : d3aaef43-c5ab-4359-9bc7-76d373a4ffde
         * userName : 测试企业一零
         * personId : 1e6d532f-fbd2-44c4-8502-005412b3e05E
         * personName : 测试企业一零主管
         * mobile : 18910248213
         * userType : CORE
         * userState : NORMAL
         * userAuthState : AUTH
         * personPosition : 真是职务maomao
         * appId : null
         * portraitFile : d25b93ae-cefe-48ee-bf22-f0ac4e9c5351
         * roleKey : ROLE_CORE_DIRECTOR
         * roleName : 核心企业主管角色
         * idType : FOREIGN_PASSPORT
         * idCode : 6523215
         * idTypeString : 外国护照
         */

        private String userId;
        private String userName;
        private String personId;
        private String personName;
        private String mobile;
        private String userType;
        private String userState;
        private String userAuthState;
        private String personPosition;
        private Object appId;
        private String portraitFile;
        private String roleKey;
        private String roleName;
        private String idType;
        private String idCode;
        private String idTypeString;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPersonId() {
            return personId;
        }

        public void setPersonId(String personId) {
            this.personId = personId;
        }

        public String getPersonName() {
            return personName;
        }

        public void setPersonName(String personName) {
            this.personName = personName;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getUserType() {
            return userType;
        }

        public void setUserType(String userType) {
            this.userType = userType;
        }

        public String getUserState() {
            return userState;
        }

        public void setUserState(String userState) {
            this.userState = userState;
        }

        public String getUserAuthState() {
            return userAuthState;
        }

        public void setUserAuthState(String userAuthState) {
            this.userAuthState = userAuthState;
        }

        public String getPersonPosition() {
            return personPosition;
        }

        public void setPersonPosition(String personPosition) {
            this.personPosition = personPosition;
        }

        public Object getAppId() {
            return appId;
        }

        public void setAppId(Object appId) {
            this.appId = appId;
        }

        public String getPortraitFile() {
            return portraitFile;
        }

        public void setPortraitFile(String portraitFile) {
            this.portraitFile = portraitFile;
        }

        public String getRoleKey() {
            return roleKey;
        }

        public void setRoleKey(String roleKey) {
            this.roleKey = roleKey;
        }

        public String getRoleName() {
            return roleName;
        }

        public void setRoleName(String roleName) {
            this.roleName = roleName;
        }

        public String getIdType() {
            return idType;
        }

        public void setIdType(String idType) {
            this.idType = idType;
        }

        public String getIdCode() {
            return idCode;
        }

        public void setIdCode(String idCode) {
            this.idCode = idCode;
        }

        public String getIdTypeString() {
            return idTypeString;
        }

        public void setIdTypeString(String idTypeString) {
            this.idTypeString = idTypeString;
        }

        @Override
        public String toString() {
            return "UserBean{" +
                    "userId='" + userId + '\'' +
                    ", userName='" + userName + '\'' +
                    ", personId='" + personId + '\'' +
                    ", personName='" + personName + '\'' +
                    ", mobile='" + mobile + '\'' +
                    ", userType='" + userType + '\'' +
                    ", userState='" + userState + '\'' +
                    ", userAuthState='" + userAuthState + '\'' +
                    ", personPosition='" + personPosition + '\'' +
                    ", appId=" + appId +
                    ", portraitFile='" + portraitFile + '\'' +
                    ", roleKey='" + roleKey + '\'' +
                    ", roleName='" + roleName + '\'' +
                    ", idType='" + idType + '\'' +
                    ", idCode='" + idCode + '\'' +
                    ", idTypeString='" + idTypeString + '\'' +
                    '}';
        }
    }

    public static class ProductsBean {
        /**
         * RECEIVABLE : true
         * ORDER : false
         * CREDIT : true
         */

        private boolean RECEIVABLE;
        private boolean ORDER;
        private boolean CREDIT;

        public boolean isRECEIVABLE() {
            return RECEIVABLE;
        }

        public void setRECEIVABLE(boolean RECEIVABLE) {
            this.RECEIVABLE = RECEIVABLE;
        }

        public boolean isORDER() {
            return ORDER;
        }

        public void setORDER(boolean ORDER) {
            this.ORDER = ORDER;
        }

        public boolean isCREDIT() {
            return CREDIT;
        }

        public void setCREDIT(boolean CREDIT) {
            this.CREDIT = CREDIT;
        }

        @Override
        public String toString() {
            return "ProductsBean{" +
                    "RECEIVABLE=" + RECEIVABLE +
                    ", ORDER=" + ORDER +
                    ", CREDIT=" + CREDIT +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "SampleRespBean{" +
                "accessToken='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                ", currentLoginDate='" + currentLoginDate + '\'' +
                ", user=" + user +
                ", bindFlag=" + bindFlag +
                ", products=" + products +
                ", messageCount=" + messageCount +
                '}';
    }
}
