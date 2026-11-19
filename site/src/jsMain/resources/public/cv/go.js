// /cv → /cv.pdf. replace() keeps the redirect page out of the back history.
location.replace('/cv.pdf' + location.search);
