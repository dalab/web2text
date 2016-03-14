Template.browser.helpers({
  loading: function () {
    return Template.instance().state.get('loading');
  }
});


Template.browser.onCreated(function () {
  // Validate input data
  const data = Template.currentData();
  check(data,{labels: [Match.OneOf(0,1)], blockedContent: String, zoom: Number, labelUpdate: Match.Optional(Function)});

  this.state = new ReactiveDict();
  this.state.setDefault({loading: true});

  this.labels         = new ReactiveVar([]);
  this.blockedContent = new ReactiveVar("");
  this.zoom           = new ReactiveVar(1);
  this.autorun(() => {
    if (!_.isEqual(this.labels.get(), Template.currentData().labels)) {
      this.labels.set(Template.currentData().labels);
    }
    this.blockedContent.set(Template.currentData().blockedContent);
    this.zoom.set(Template.currentData().zoom);
  });
});

Template.browser.onRendered(function () {
  console.log("Render page");
  const iframe       = this.find(".browser-window");
  const idoc         = iframe.contentDocument;
  let _zoomStyleNode = null;

  this.autorun(() => {
    this.state.set({'loading': true});

    // Set iframe content
    idoc.open();
    idoc.write(this.blockedContent.get());
    idoc.close();
    iframe.onload = () => this.state.set('loading',false);

    // Make iFrame safe for use
    PageBlocks.deactivateLinks(idoc);
    PageBlocks.unblockStyles(idoc);
    PageBlocks.addStyleString(idoc, PageBlocks.pageCss);

    _zoomStyleNode = PageBlocks.addStyleString(
      idoc,
      PageBlocks.zoomStyle(
        Tracker.nonreactive(() => Session.get('_zoomLevel'))
      )
    );
  });

  // Attach zoom listener
  this.autorun(() => {
    console.log("Set zoom level");
    _zoomStyleNode.innerHTML =
      PageBlocks.zoomStyle(this.zoom.get());
  });

  // Labels (reactive)
  this.autorun(() => {
    console.log("Set labels");
    PageBlocks.markBlocks(idoc, this.labels.get());
  });


  if (this.data.labelUpdate) {
    _attachKeyListener(idoc);
    _attachKeyListener(window);
    this._initialW = 0;
    this._initialH = 0;
    this._selectionBox = null;
    _attachDragHandlers.call(this,idoc);
  }

});

let _attachKeyListener = (to) => {
  const keycode = {ctrl: 17, alt: 18, A: 65};
  to.onkeyup = function(e) {
    if (e.keyCode === keycode.ctrl || e.keyCode === keycode.alt)
      Session.set("_altPressed",false);
  }
  to.onkeydown = function(e) {
    if (e.keyCode === keycode.ctrl || e.keyCode === keycode.alt)
      Session.set("_altPressed",true);
  }
};

const _attachDragHandlers = function (dom) {
  const labelUpdate = this.data.labelUpdate;
  const $ = jQuery;

  $(dom).mousedown(function (e) {

    e.preventDefault();

    if (this._selectionBox) dom.body.removeChild(this._selectionBox);
    this._selectionBox = dom.createElement('div');
    this._selectionBox.style.position = 'absolute';

    if (!Session.get('_altPressed')) this._selectionBox.className = 'selection add';
    else                             this._selectionBox.className = 'selection remove';

    dom.body.appendChild(this._selectionBox);
    dom.body.style.cursor = 'default';

    this._initialW = e.pageX; this._initialH = e.pageY;

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
    const width  = Math.abs(this._initialW - e.pageX)/zoom,
          height = Math.abs(this._initialH - e.pageY)/zoom,
          left   = Math.min(this._initialW, e.pageX)/zoom,
          top    = Math.min(this._initialH, e.pageY)/zoom;

    let style = this._selectionBox.style

    style.width  = `${width}px`;
    style.height = `${height}px`;
    style.left   = `${left-dom.body.offsetLeft}px`;
    style.top    = `${top-dom.body.offsetTop}px`;
  }

  function selectElements(e) {
    $(dom).unbind("mousemove", openSelector);
    $(dom).unbind("mouseup", selectElements);

    dom.body.removeChild(this._selectionBox);
    this._selectionBox = null;

    const width  = Math.abs(this._initialW - e.pageX),
          height = Math.abs(this._initialH - e.pageY),
          left   = Math.min(this._initialW, e.pageX),
          top    = Math.min(this._initialH, e.pageY),
          right  = Math.max(this._initialW, e.pageX),
          bottom = Math.max(this._initialH, e.pageY);

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
                           this._initialW >= rl && this._initialW <= rr &&
                           this._initialH >= rt && this._initialH <= rb);

        if (inside || clickTest) match = true;
      });
      return match;
    }).forEach(b => {
      let tag;
      if (!Session.get("_altPressed")) tag = 1;
      else                             tag = 0;

      const block_id = $(b).data('id');

      updates.push({block_id,tag});
    });
    labelUpdate(updates);
  }

};
