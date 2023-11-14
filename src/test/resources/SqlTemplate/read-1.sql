select id,
       name,
       age,
       sex,
       score,
       class,
       comment
from a.b
where name like '%{p_expression}%'
  and (comment = '{{GOOD}}' or common in ({null_comment}))
  and sex = {p_string}
  and class in {p_array_of_string}
  and ags in {p_array_if_int}