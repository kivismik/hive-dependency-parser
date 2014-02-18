-- select test

use p;

from
  ( select y
  from
    a
    join
    ( select z from b ) t
    on (a.w = t.z)
  where
    z = 0 and w is not null
  ) t

insert overwrite table q.a
select x
where f(x) = "s"

insert into table q.b
select y
where f(y) = "t"
;
