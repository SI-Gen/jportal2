@if not exist %2\%1 goto endoff
  @C:\vslick\win\vsdiff %1 %2\%1
  :xxx@pause
:endoff

