@param String username
@param String uuid
<article data-signals="{u:'${uuid}',n:'${username}'}" data-effect="localStorage.setItem('kissUuid',$u);localStorage.setItem('kissUser',$n);setTimeout(function(){location.href='/showcase';},700);">
  <p class="success-text"><strong>Welcome, ${username}!</strong></p>
  <p>Taking you to the dashboard&hellip;</p>
</article>
