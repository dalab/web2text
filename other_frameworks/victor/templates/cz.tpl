### Victor CRF++ template file

# features of the blocks themselves
U$n:%x[{-3,-2,-1,0,1,2,3}:sentence.count]
U$n:%x[{-3,-2,-1,0,1,2,3}:token.alpha-abs]
U$n:%x[{-3,-2,-1,0,1,2,3}:token.alpha-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:token.num-abs]
U$n:%x[{-3,-2,-1,0,1,2,3}:token.num-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:token.mixed-abs]
U$n:%x[{-3,-2,-1,0,1,2,3}:token.mixed-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:token.other-abs]
U$n:%x[{-3,-2,-1,0,1,2,3}:token.other-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:char.alpha-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:char.num-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:char.punct-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:char.white-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:char.other-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:avg-word-length]
U$n:%x[{-3,-2,-1,0,1,2,3}:avg-word-run]

U$n:%x[{-3,-2,-1,0,1,2,3}:container.class-bold]
U$n:%x[{-3,-2,-1,0,1,2,3}:container.class-italic]
U$n:%x[{-3,-2,-1,0,1,2,3}:container.class-headers]
U$n:%x[{-3,-2,-1,0,1,2,3}:container.class-lists]
U$n:%x[{-3,-2,-1,0,1,2,3}:container.class-forms]
U$n:%x[{-3,-2,-1,0,1,2,3}:container.p]
U$n:%x[{-3,-2,-1,0,1,2,3}:container.a]
U$n:%x[{-3,-2,-1,0}:container.img]

U$n:%x[{-3,-2,-1,0,1,2,3}:regexp.url]
U$n:%x[{-3,-2,-1,0,1,2,3}:regexp.date]
U$n:%x[{-3,-2,-1,0,1,2,3}:regexp.time]

U$n:%x[{-3,-2,-1,0,1,2,3}:twins]
U$n:%x[0:first-twin]
U$n:%x[{-3,-2,-1,0,1,2,3}:td-group.word-rel]
U$n:%x[{-3,-2,-1,0,1,2,3}:div-group.word-rel]

# U$n:%x[{-3,-2,-1,0,1,2,3}:langid1]
# U$n:%x[{-3,-2,-1,0,1,2,3}:langid2]

# global features
Uglobal_$n:%x[0:document.word-count]
Uglobal_$n:%x[0:document.sentence-count]
Uglobal_$n:%x[0:document.block-count]
Uglobal_$n:%x[0:document.max-td-group]
Uglobal_$n:%x[0:document.max-div-group]

U$n:%x[0:position]


# properties of the distance to the neighbour blocks
Udist_$n:%x[{-2,-1,0,1,2,3}:sentence-not-started]
Udist_$n:%x[{-2,-1,0,1,2,3}:sentence-not-finished]
Udist_$n:%x[{-2,-1,0,1,2,3}:split.class-inline]
Udist_$n:%x[{-2,-1,0,1,2,3}:split.class-block]
Udist_$n:%x[{-2,-1,0,1,2,3}:split.class-{inline,block}]
Udist_$n:%x[{-2,-1,0,1,2,3}:split.p]
Udist_$n:%x[{-2,-1,0,1,2,3}:split.br]
Udist_$n:%x[{-2,-1,0,1,2,3}:split.hr]



#B
