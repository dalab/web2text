PageBlocks = {
  pageCss: `
    .boilerplate-text-block {
      background-color: rgba(255,255,0,0.5);
      border:1px solid #bbb;
    }
    .boilerplate-text-block.keep {
      background-color: #bada55 !important;
      color: black !important
    }
    .boilerplate-text-block.remove{
      background-color:red !important;
      color:white !important;
    }
    .selection {
      background-color: rgba(186,218,85,.5)
    }
    .selection.remove {
      background-color: rgba(255,0,0,0.5)
    }
    body, body * {
      -webkit-touch-callout: none;
      -webkit-user-select: none;
      -khtml-user-select: none;
      -moz-user-select: none;
      -ms-user-select: none;
      user-select: none;
    }`,

  markBlocks( dom, labels ) {
    let blocks = this.getBlocks(dom);
    [].forEach.call(blocks, (b,i) => {
      if (labels[i] == 1) {
        b.className = "boilerplate-text-block keep";
      } else {
        b.className = "boilerplate-text-block remove";
      }
    });
  },

  disableImageDrag(dom) {
    $(dom).find('img').mousedown((e) => e.preventDefault());
  },

  unblockStyles(dom) {
    $(dom).find('link[rel=stylesheet][media=print]').remove();
    [].forEach.call($(dom).find('link[rel=stylesheet]'),(node) => {
      node.setAttribute("media","none");
      node.setAttribute('onload',"if(media!='all')media='all'");
    });
  },

  getBlocks(dom) {
    return dom.getElementsByClassName('boilerplate-text-block')
  },

  deactivateLinks( idoc ) {
    [].forEach.call(idoc.getElementsByTagName('a'), (el) => {
      el.addEventListener('click', (e) => {
        e.preventDefault();
      });
    });
    [].forEach.call(idoc.getElementsByTagName('button'), (el) => {
      el.addEventListener('click', (e) => {
        e.preventDefault();
      });
    });
    [].forEach.call(idoc.getElementsByTagName('form'), (el) => {
      el.addEventListener('submit', (e) => {
        e.preventDefault();
      });
    });
  },

 addStyleString( dom, str ) {
    var node = document.createElement('style');
    node.innerHTML = str;
    dom.body.appendChild(node);
    return node;
  },

  zoomStyle(zoom) {
    return `body { transform: scale(${zoom});
     transform-origin: 0% 0; margin:0; padding:0.1px;}`;
  }

};
