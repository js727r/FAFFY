<!--
작성자 : 박윤하
프로필 뷰 - 작성 게시글 목록 페이지
-->
<template>
  <div id="board">
    <div>
      <div id="route">
        <p class="text-h6" style="font-weight: 600">
          <router-link :to="{ name: 'profile' }"> 정보 </router-link>
        </p>
        <v-icon color="black" class="mb-4"> mdi-chevron-right </v-icon>
        <p class="text-h6" style="font-weight: 600"> 작성 게시글 </p>
      </div>
      <div id="content" class="mt-4">
        <p class="text-h6">
          <v-tabs color="0c0f66">
            <v-tab
              :id="selected === 1 ? 'selectedBtn' : 'selectBtn'"
              @click="selected = 1"
              >자유</v-tab
            >
            <v-tab
              :id="selected === 2 ? 'selectedBtn' : 'selectBtn'"
              @click="selected = 2"
              >Q&A</v-tab
            >
            <v-tab
              :id="selected === 3 ? 'selectedBtn' : 'selectBtn'"
              @click="selected = 3"
              >정보</v-tab
            >
          </v-tabs>
        </p>

        <div>
          <!-- 카테고리별 작성 게시글 리스트 selected로 카테고리 구분해서 출력할 것 -->
          <v-simple-table v-if="boardType.length != 0">
            <template v-slot:default>
              <thead>
                <tr>
                  <th class="text-start" width="65%">
                    제목
                  </th>
                  <th class="text-start" width="10%">
                    댓글
                  </th>
                  <th class="text-start" width="15%">
                    작성 일자
                  </th>
                  <th class="text-start" width="10%">
                    조회수
                  </th>
                </tr>
              </thead>
              <!--각각의 게시글, 클릭하면 상세조회로 이동-->
              <tbody>
                <tr
                  v-for="(board,i) in boardType"
                  :key="i"
                  style="cursor: pointer"
                  @click="boardDetail(board.no)"
                >
                  <td>{{ board.title }}</td>
                  <td><v-icon small> mdi-comment-processing-outline </v-icon> {{ board.commentCount }}</td>
                  <td>{{ board.dateTime.replaceAll('-', '.').slice(0, 10) }}</td>
                  <td><v-icon small> mdi-eye </v-icon> {{ board.hit }}</td>
                </tr>
              </tbody>
            </template>
          </v-simple-table>

          <v-col v-else style="height:600px; display:flex; justify-content:center; align-items:center;" cols="12">
            <div style="text-align:center;">
              <v-icon color="#333" large block> mdi-clipboard-text-off-outline </v-icon>
              <h4>{{ nodata }}</h4>
            </div>
          </v-col>

          <!-- pagination -->
          <div v-if="boardType.length != 0" class="text-center pt-4">
            <v-pagination
              circle
              color="#0c0f66"
              v-model="page"
              :length="pageCount"
              :total-visible="5"
            ></v-pagination>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex';
const profileStore = "profileStore";

export default {
  name: "BoardView",
  data() {
    return {
      headers: [
        { text: "제목", align: "start", value: "title" },
        { text: "댓글 수", value: "commentCount" },
        { text: "작성 일자", value: "dateTime" },
        { text: "조회수", value: "hit" },
      ],

      // pagination 관련 변수
      page: 1,
      pageCount: 0,
      itemsPerPage: 10,
      boardDate: [],

      // 카테고리 판별하는 변수 자유 : 1, Q&A : 2, 정보 : 3
      selected: 1,
      types: {1: 'Free', 2: 'QnA', 3: 'Info'},
      nodata: '작성한 게시글이 없습니다.'
    };
  },
  methods: {
    boardDetail(e) {
      this.$router.push({ name: 'board-detail', params: { boardNo: e.no }})
    }
  },
  computed: {
    ...mapGetters(profileStore, ['userBoardList']),
    boardType () {
      return this.userBoardList.filter(board => board.category === this.types[this.selected])
    }
  },
  watch: {
    selectd() {
      for (var i = 0; i < this.userBoardList.length; i++) {
        this.boardDate[i] = this.latestBoardList[i].dateTime = this.latestBoardList[i].dateTime.substr(0, 10).replaceAll('-','.');
      }
    }
  }
};
</script>

<style scoped>
#board {
  background-color: #fff;
  padding: 5%;
  padding-bottom: 2%;
  text-align: left;
  height: 100%;
  display: flex;
  justify-content: start;
  align-items: flex-start;
  cursor: default;
}

#route {
  display: flex;
  justify-content: start;
}

a {
  color: black;
  text-decoration: none;
}

#board div {
  width: 100%;
}

#content {
  background-color: #fff;
  padding: 2%;
}

#selectBtn {
  background-color: #fff;
  color: #0c0f66;
  font-weight: 600;
  font-size: 16px;
}

#selectedBtn {
  background-color: #0c0f66;
  color: #fff;
  font-weight: 600;
  font-size: 16px;
}

#table {
  height: 520px;
}

button:before {
  background-color: transparent;
}
.row-pointer > .v-data-table__wrapper > table > tbody > tr:hover {
  cursor: pointer;
}
</style>
