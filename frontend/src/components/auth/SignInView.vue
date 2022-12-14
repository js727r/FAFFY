<!--
작성자 : 박윤하
로그인 페이지
-->
<template>
  <v-container>
    <v-form ref="form" id="signIn" @submit.prevent="requestSignIn">
      <img src="@/assets/images/faffy_logo_big.png" alt="faffy logo" />
      <!-- 이메일 입력 -->
      <v-text-field
        v-model="form.email"
        type="email"
        label="이메일"
        required
        color="#0c0f66"
      />

      <!-- 비밀번호 입력 -->
      <v-text-field
        v-model="form.password"
        :append-icon="type ? 'mdi-eye' : 'mdi-eye-off'"
        :type="type ? 'text' : 'password'"
        label="비밀번호"
        required
        color="#0c0f66"
        @click:append="type = !type"
      />

      <dark-button :btnValue="signInValue" @click="requestSignIn" />

      <!-- 회원가입 및 비밀번호 찾기  -->
      <div id="route" class="mt-4">
        <div type="button" @click="goTo">회원가입</div>

        <v-dialog
          v-model="dialog"
          width="500"
        >
          <template v-slot:activator="{ on, attrs }">
            <div type="button"
              v-bind="attrs"
              v-on="on"
            >
              비밀번호 찾기
            </div>
          </template>
          <v-card>
            <v-card-title id="dialogTitle">
              <img class="ml-5" src="@/assets/images/faffy_logo_big.png" alt="faffy logo" />
            </v-card-title>

            <v-card-text>
              <v-form ref="form" @submit.prevent="findPassword">
                <!-- 이메일 입력 -->
                <v-text-field
                  v-model="find.email"
                  type="email"
                  label="이메일"
                  required
                  color="#0c0f66"
                />

                <!-- 이름 입력 -->
                <v-text-field
                  v-model="find.name"
                  type="text"
                  label="이름"
                  required
                  color="#0c0f66"
                />

                <dark-button class="mt-4" :btnValue="findValue" @click="findPassword" />
              </v-form>
              <v-btn
                id="closeBtn"
                class="mt-4 mb-2"
                block
                rounded
                elevation="0"
                @click="dialog = false"
              >닫기</v-btn>
            </v-card-text>
          </v-card>
        </v-dialog>
      </div>

      <!-- 소셜 로그인 -->
      <hr />

      <div id="social">
        <!-- 네이버 로그인 -->
        <v-btn fab elevation="0" class="overflow-hidden" @click="naverLogin">
          <img
            src="@/assets/images/naver_login.png"
            alt=""
            style="width: 60px; height: 60px"
          />
        </v-btn>

        <!-- 구글 로그인 -->
        <v-btn fab elevation="0" class="overflow-hidden" @click="googleLogin">
          <img
            src="@/assets/images/google_login.png"
            alt=""
            style="width: 80px; height: 80px"
          />
        </v-btn>
      </div>
    </v-form>
  </v-container>
</template>

<script>
import DarkButton from "@/components/common/DarkButton.vue";
import { mapState, mapMutations,mapActions } from "vuex";
import { auth } from "@/api/auth.js";
const authStore = "authStore";

export default {
  name: "SignIn",
  components: {
    DarkButton,
  },
  data() {
    return {
      form: {
        email: "",
        password: "",
      },

      find: {
        email: "",
        name: "",
      },

      type: false,

      dialog: false,

      signInValue: "로그인",
      findValue: "비밀번호 찾기",
    };
  },
  computed: {
    ...mapState(authStore,["isLogin","loginUser"]),
  },
  methods: {
    ...mapMutations(authStore, [
      "SET_IS_LOGIN",
      "SET_USER_INFO",
      "SET_FOLLOWING_LIST",
      "SET_NAVER_STATE",
    ]),
    ...mapActions(authStore,["loadFollowing"]),
    async save() {
      this.$refs.form.validate();
      await this.$nextTick();
      if (!this.valid) return;
    },
    goTo() {
      this.$router.push({ name: "sign-up" });
    },
    findPassword() {
      auth.findPwd(
        this.find.email, this.find.name,
        () => {
          alert("임시 비밀번호가 발송되었습니다. 이메일을 확인해주세요.")
        },
        () => {
          this.$dialog.message.info('올바른 이메일 주소인지 및 이름을 확인하세요.', {
            position: "top",
            timeout: 2000,
            color: "#ff7451",
          });
        }
      )
    },
    async requestSignIn() {
      await auth.login(
        this.form,
        (response) => {
          const accessToken = response.data.content["token"];
          sessionStorage.setItem("X-AUTH-TOKEN", accessToken);

          const loginUser = response.data.content["user"];
          this.SET_USER_INFO(loginUser);
          this.$router.push({ name: "main" });
        },
        () => {
          this.$dialog.message.info('로그인에 실패했습니다. 아이디 및 비밀번호를 확인하세요.', {
            position: "top",
            timeout: 2000,
            color: "#ff7451",
          });
        }
      );
      if (!this.isLogin)
        return;

      await this.loadFollowing();
    },
    googleLogin(){
      auth.socialLogin(
        "google",
        (response) => {
          window.location.href= response.data.redirectURL;
        },
        () => {
        }
      )
    },
    naverLogin(){
      auth.socialLogin(
        "naver",
        (response) => {
          this.SET_NAVER_STATE(response.data.state);
          window.location.href= response.data.redirectURL;
        },
        () => {
        }
      )
    }
  },
  metaInfo() {
    return {
      meta: [
        { charset: "utf-8" },
        { name: "viewport", content: "width=device-width, initial-scale=1" },
      ],
    };
  },
};
</script>

<style scoped>
.container {
  background-color: white;
  padding: 5%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
}

#signIn {
  width: 30%;
  margin: 0 auto;
}

#route {
  display: flex;
  justify-content: space-between;
  color: #0c0f66;
}

hr {
  border-top: 2px solid #0c0f66;
  text-align: center;
  margin-top: 40px;
  margin-bottom: 30px;
}

hr:after {
  content: "소셜 로그인";
  display: inline-block;
  position: relative;
  top: -13px;
  padding: 0 10px;
  background: #fff;
  color: #0c0f66;
  font-size: 16px;
}

#signInBtn {
  background-color: #0c0f66;
  color: #fff;
}

#dialogTitle {
  justify-content : center;
}

#closeBtn {
  background-color: #ff7451;
  color: #fff;
}

#social {
  display: flex;
  justify-content: space-around;
}
</style>
