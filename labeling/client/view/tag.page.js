'use strict';

let _state = () => Iron.controller().state;

let _altPressed = false;

Meteor.startup(() => {
  Session.set("_altPressed",false);
  Session.set("_zoomLevel",1);
});

Template.tagPage.helpers({
  'isRemoving': function () {
    return Session.get("_altPressed");
  },
  'zoomLevel': function () {
    return Math.round(Session.get("_zoomLevel")*100);
  }
});

Template.tagPage.events({
  'click .next-tag-page'(evt) {
    evt.preventDefault();
    let {dataset, doc_id} = Documents.findOne(
      {'doc_id': {'$gt': this.doc_id}},
      {sort: {'doc_id':1}}
    );
    if (!page) return;
    Router.go('tag.page',
              {dataset_id: dataset, id: doc_id} );
  },

  'input #zoom-level, change #zoom-level'(evt) {
    console.log('thiny has changed');
    Session.set('_zoomLevel',$(evt.target).val()/100);
  },

  'click .prev-tag-page'(evt) {
    evt.preventDefault();
    let {dataset, doc_id} = Documents.findOne(
      {'doc_id': {'$lt': this.doc_id}},
      {sort: {'doc_id':-1}}
    );
    if (!page) return;
    Router.go('tag.page',
              {dataset_id: dataset, id: doc_id});
  }
});

Template.tagPage.rendered = function() {

  const iframe = document.getElementById("page");
  const idoc = iframe.contentDocument;
  idoc.write(this.data.blocked_source);

  const doc_id = this.data.doc_id;

  this.autorun(() => {
    const data = Router.current().data();
    const label_name = "user-" + Meteor.userId();
    const labelsEntry = Labels.findOne({label_name, doc_id: data.doc_id});
    if (!labelsEntry) {
      Meteor.call('setLabels',data.dataset, doc_id,_initLabels(PageBlocks.getBlocks(idoc)));
      return;
    }
    const labels = labelsEntry.labels
    PageBlocks.markBlocks(idoc, labels);
  });

  const _zoomStyleNode = PageBlocks.addStyleString(idoc,PageBlocks.zoomStyle(1));
  this.autorun(() => {
    console.log("set zoom level");
    _zoomStyleNode.innerHTML = PageBlocks.zoomStyle(Session.get('_zoomLevel'));
  });

  _attachKeyListener(idoc);
  _attachKeyListener(window);

  _attachDragHandlers(idoc, doc_id);

  PageBlocks.deactivateLinks(idoc);
  PageBlocks.addStyleString(idoc, PageBlocks.pageCss);

};

let _attachKeyListener = (to) => {

  const keycode = {alt: 18, A: 65};

  to.onkeyup = function(e) {
    if (e.keyCode == keycode.alt)
      Session.set("_altPressed",false);
  }
  to.onkeydown = function(e) {
    if (e.keyCode == keycode.alt)
      Session.set("_altPressed",true);
    if (e.keyCode == keycode.A) {
      // mark all as ...
    }
  }
};

let _initialW = 0, _initialH = 0, _selectionBox = null;

let _attachDragHandlers = (dom, doc_id) => {

  const $ = jQuery;

  $(dom).mousedown(function (e) {

    e.preventDefault();

    if (_selectionBox) dom.body.removeChild(_selectionBox);
    _selectionBox = dom.createElement('div');
    _selectionBox.style.position = 'absolute';

    if (!Session.get('_altPressed')) _selectionBox.className = 'selection add';
    else                             _selectionBox.className = 'selection remove';

    dom.body.appendChild(_selectionBox);
    dom.body.style.cursor = 'default';

    _initialW = e.pageX; _initialH = e.pageY;

    $(dom).bind('mouseup', selectElements);
    $(dom).bind('mousemove', openSelector);

  });

  function drawRect(rect) {
    const tableRectDiv = dom.createElement('div');

    const scrollTop = dom.documentElement.scrollTop || dom.body.scrollTop,
          scrollLeft = dom.documentElement.scrollLeft || dom.body.scrollLeft;
    let style = tableRectDiv.style;
    style.position  = 'absolute';
    style.border    = '1px solid blue';
    style.margin    = '0';
    style.padding   = '0';
    style.top       = (rect.top + scrollTop) + 'px';
    style.left      = (rect.left + scrollLeft) + 'px';
    style.width     = (rect.width - 2) + 'px';
    style.height    = (rect.height - 2) + 'px';

    dom.body.appendChild(tableRectDiv);
  }

  function openSelector(e) {
    const zoom = Session.get('_zoomLevel');
    const width  = Math.abs(_initialW - e.pageX)/zoom,
          height = Math.abs(_initialH - e.pageY)/zoom,
          left   = Math.min(_initialW, e.pageX)/zoom,
          top    = Math.min(_initialH, e.pageY)/zoom;

    let style = _selectionBox.style
    console.log(dom.body,dom.body.offsetLeft, dom.body.offsetTop);
    style.width  = `${width}px`;
    style.height = `${height}px`;
    style.left   = `${left-dom.body.offsetLeft}px`;
    style.top    = `${top-dom.body.offsetTop}px`;
  }

  function selectElements(e) {
    $(dom).unbind("mousemove", openSelector);
    $(dom).unbind("mouseup", selectElements);

    dom.body.removeChild(_selectionBox);
    _selectionBox = null;

    const width  = Math.abs(_initialW - e.pageX),
          height = Math.abs(_initialH - e.pageY),
          left   = Math.min(_initialW, e.pageX),
          top    = Math.min(_initialH, e.pageY),
          right  = Math.max(_initialW, e.pageX),
          bottom = Math.max(_initialH, e.pageY);

    dom.body.style.cursor = "automatic";

    let updates = [];
    [].filter.call(PageBlocks.getBlocks(dom), b => {
      const rectCollection = b.getClientRects();
      let match = false;
      [].forEach.call(rectCollection, (r) => {

        const rl = r.left   + $(dom).scrollLeft(),
              rr = r.right  + $(dom).scrollLeft(),
              rt = r.top    + $(dom).scrollTop(),
              rb = r.bottom + $(dom).scrollTop();

        const inside = (left <= rr && right >= rl
                        && top <= rb && bottom >= rt);

        const clickTest = (width == 0 && height == 0 &&
                           _initialW >= rl && _initialW <= rr &&
                           _initialH >= rt && _initialH <= rb);

        if (inside || clickTest) match = true;
      });
      return match;
    }).forEach(b => {
      let tag;
      if (!Session.get("_altPressed")) tag = 1;
      else                             tag = 0;

      const block_id = $(b).data('id');

      // Meteor.call('tagBlock', tag, doc_id, block_id);
      updates.push({block_id,tag});
    });
    Meteor.call('tagBlocks', doc_id, updates);
  }

};



let _initLabels = (blocks) => {
  return [].map.call(blocks, x => 0);
};