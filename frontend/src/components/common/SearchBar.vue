<!--
작성자 : 박윤하
검색 바
-->
<template>
  <div id="searchBar">
    <v-text-field
      hide-details
      placeholder="검색"
      v-model="keyword"
      color="#0c0f66"
      filled
      rounded
      dense
      single-line
      append-icon="mdi-magnify" class="shrink mx-4"
      @click:append="onInputKeyword"
      @keydown.enter="onInputKeyword"
      @keyup.tab="onInputKeyword"
    />
  </div>
</template>

<script>
export default {
  name: 'SearchBar',
  data() {
    return {
      keyword: "",
    }
  },
  methods: {
    onInputKeyword(e) {
      e.target.value = '';
      this.$router.push({ name: 'search', params: { keyword: this.keyword }})
      // 키워드랑 같이 검색 결과 페이지로 이동, 이동 후 키워드 삭제
      this.keyword = '';
    }
  }
}
</script>

<style scoped>
#searchBar {
  width: 30vw !important;
}
</style>
