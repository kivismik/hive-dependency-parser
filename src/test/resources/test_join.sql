use p;

select
  *
from
  a join b on (a.x = b.y)
    join c on (a.x = c.z)
    join d on (a.x = d.w)
;