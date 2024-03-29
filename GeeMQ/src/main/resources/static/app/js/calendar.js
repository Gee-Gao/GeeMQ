/**
 * 移动webapp开发 日历组件
 * 可用于需要日历选择的场景
 *  - 日历范围选择
 *  - 节假日显示
 *  - 自由配置初始化生成多日历
 *  - 各种操作事件自由监听
 * @author samczhang@tencent.com
 * ----------------------------------------------
 * 对外调用接口及自定义事件
 * @method render 渲染日历
 * @method nextMonth 上一月
 * @method prevMonth 下一月
 * @method show 显示日历
 * @method hide 隐藏日历
 * @method setSelectDate 设置当前选中日期

 * @customEvent selectDate 选择日期时派发事件
 * @customEvent show 显示日历时派发事件
 * @customEvent hide 隐藏日历时派发事件
 */

( function( root, factory ) {
    if ( typeof define === 'function' ) {
        define( 'calendar', [ 'mui' ], function( $ ) {
            return factory( root, $ );
        } );
    } else {
        root.calendar = factory( root, root.mui );
    }
} )( window, function( root, $) {

    var util = {
        /**
         * 根据当前年月，计算下一个月的年月
         * @para year {int} eg: 2014 YYYY
         * @para month {int} eg: 12 MM/M
         * @return {object} 
         */
        getNextMonthDate: function( year, month ) {
            if ( month > 12 ) {
                year = year + Math.floor( ( month - 1 ) / 12 );

                if ( month % 12 == 0 ) {
                    month = 12;
                } else {
                    month = month % 12;  
                }
            }

            return {
                year: year,
                month: month
            }
        },

        /**
         * 处理小于10的数字前自动加0
         * @para num {num} int
         * return {string} '02'
         */
        formatNum: function( num ) {
            if ( num < 10 ) {
                num = '0' + num;
            }

            return num;
        },

        /**
         * 连接年月日，连接符为`-`
         * return {string} yyyy-mm-dd
         */
        joinDate: function( year, month, day ) {
            var formatNum = util.formatNum;

            return year + '-' + formatNum( month ) + '-' + formatNum( day );
        },

        /**
         * 将格式化后日期转化为数字，便于日期计算
         * @para date {string|date object} yyyy-mm-dd|日期对象
         * return {string} yyyymmdd
         */
        dateToNum: function( date ) {
            var rst = '';
            if ( typeof date == 'string' ) {
                rst = date.split( '-' ).join( '' );
            } else {
                rst = util.formatDate( date ).split( '-' ).join( '' );
            }

            return rst;
        },
		
		numToDate: function( num ) {
			var str = num.toString();
			return str.substr(0, 4) + '-' + str.substr(4, 2) + '-' + str.substr(6);
		},
		
        /**
         * 格式化日期对象
         * @para {date object} 日期对象
         * return {string} yyyy-mm-dd
         */
        formatDate: function( dateObj ) {
            var year = dateObj.getFullYear(),
                month = dateObj.getMonth() + 1,
                day = dateObj.getDate();

            return util.joinDate( year, month, day );
        },

        /**
         * 获取当前日期的下一天
         * @para date {string|date object} yyyy-mm-dd|日期对象
         * @para num {int} 获取指定日期之后的几天
         * return {string} yyyy-mm-dd
         */
        getNextDate: function( date, num ) {
            return util.formatDate( new Date( +new Date( date ) + num * 86400000 ) );
        },
    };

    var tpl = [
        '<div class="cal-wrap">',
            '<h2>{%date%}</h2>',
            '{%week%}',
            '<ul class="day">',
                '{%day%}',
            '</ul>',
        '</div>'
    ].join( '' );

    var weekTpl = [
        '<ul class="week">',
            '<li>一</li>',
            '<li>二</li>',
            '<li>三</li>',
            '<li>四</li>',
            '<li>五</li>',
            '<li class="wk">六</li>',
            '<li class="wk">日</li>',
        '</ul>'
    ].join( '' );


	 mui.ajax(serverAddr + "/aunt/calc/calendar", {
	          data: {
	            "userId":"1553628272261099521"
	          },
	          dataType: 'json', //服务器返回json格式数据
	          type: 'post', //HTTP请求类型
	          timeout: 10000, //超时时间设置为10秒；
	          headers: {
	            'Content-Type': 'application/json'
	          },
	          success: function(data) {
	            var calcData = data.data.dangerousPeriod;
	            holidaysMap.push({
					name:'危险日',
					date:calcData
				})
	          },
	          error: function(xhr, type, errorThrown) {
	            //异常处理；
	            if (type == "timeout") {
	              plus.nativeUI.toast("请求超时");
	            } else {
	              plus.nativeUI.toast("请求失败");
	            }
	          }
	        });
	
    var holidaysMap = [
        
    ];

    var calendar = function( config ) {
        this.defaultConfig = {
            /**
             * 日历外层容器ID
             * type {string|object} id字符串或dom对象
             */
            el: '#calendar',

            /**
             * 初始化日历显示个数
             */
            count: 5,

            /**
             * 用于初始化日历开始年月
             * type {date object} 日期对象
             */
            date: new Date(),

            /**
             * 日期最小值，当小于此日期时日期不可点
             * type {date object} 日期对象
             */
            minDate: null,

            /**
             * 日期最大值，当大于此日期时日期不可点
             * type {date object} 日期对象
             */
            maxDate: null,  //日期对象

            /**
             * 初始化当前选中日期，用于高亮显示
             * type {date object} 日期对象
             */
            selectDate: new Date(),

            /**
             * 选中日期时日期下方文案
             * type {string}
             */
            selectDateName: '',
			
			/**
			 * 第二个选中项
			 */
			secondDate: null,
			secondDateName: '',
			
            /**
             * 是否显示节假日
             * type {boolean}
             */
            isShowHoliday: true,

            /**
             * 在日历中是否显示星期
             * type {boolean}
             */ 
            isShowWeek: true
        };

        this.config = $.extend( {}, this.defaultConfig, config || {} );
        this.el = ( typeof this.config.el === 'string' ) ? document.querySelector( this.config.el ) : this.config.el;

        this.init.call( this );
    };

    $.extend( calendar.prototype, {
        init: function() {
            this._initDate();
            this.render();
            this._initEvent();
        },

        _initDate: function() {
            var me = this,
                config = this.config,
                dateObj = config.date,
                dateToNum = util.dateToNum;

            //初始化日历年月
            this.date = dateObj;
            this.year = dateObj.getFullYear();
            this.month = dateObj.getMonth() + 1;

            this.minDate = config.minDate;
            this.maxDate = config.maxDate;
            this.selectDate = config.selectDate;
            this.secondDate = config.secondDate;

            //上下月切换步长，根据初始化日历个数决定
            this.step = config.count;
            this.selectStep = 0;
            this.selectKeys = ['selectDate', 'secondDate'];
            this.selectKey = this.selectKeys[0];
            this.selectNames = [config.selectDateName, config.secondDateName];
        },

        /**
         * 渲染日历
         */
        render: function() {
            var me = this,
                config = this.config,
                year = this.date.getFullYear(),
                month = this.date.getMonth() + 1,
                tmpTplArr = [];

            for ( var i = 0; i < config.count; i++ ) {
                var curMonth = month + i,
                    curDate = util.getNextMonthDate( year, curMonth ),
                    dateStr = curDate.year + '年' + util.formatNum( curDate.month ) + '月',
                    dayList = this._getDayList( curDate.year, curDate.month ),
                    week = '';

                if ( config.isShowWeek ) {
                    week = weekTpl;
                } 

                var curTpl = tpl.replace( '{%date%}', dateStr )
                    .replace( '{%week%}', week )
                    .replace( '{%day%}', dayList );

                tmpTplArr.push( curTpl );
            }

            this.el.innerHTML = tmpTplArr.join( '' ) ;
            this.setSelectDate( this.selectDate );
        },

        _initEvent: function() {
            var me = this,
                config = this.config;

            mui(this.el).on('tap', 'ul.day li', function( event ) {
                var curItem = this,
                    date = curItem.getAttribute( 'data-date' ),
                    dateName = curItem.querySelectorAll( 'i' )[ 1 ].innerText,
                	lastSltItems = me.el.querySelectorAll( 'li.cur' );

                //更新当前选中日期YYYY-MM-DD
                me.selectStep = lastSltItems.length == 1 && util.dateToNum(date) > util.dateToNum(me.selectDate) && me.secondDate != null ? 1 : 0;
                me.selectKey = me.selectKeys[me.selectStep];
               	me[me.selectKey] = new Date(date);

                if ( !curItem.classList.contains( 'iv' ) ) {
                	if (me.selectKey == 'selectDate') { 
                    	me.afterSelectDate && me.afterSelectDate (util.formatDate(me.selectDate));
                    } else {
                   		me.afterSecondDate && me.afterSecondDate (util.formatDate(me.secondDate));
                    }
                    me.setSelectDate( date );
                }
            });
        },

        /**
         * 根据当前年月，获取日期列表html字体串
         * @para year {int} eg: 2014 YYYY
         * @para month {int} eg: 12 MM/M
         * @return {string}
         */
        _getDayList: function( year, month ) {
            var me = this,
                config = this.config,

                days = new Date( year, month, 0 ).getDate(),
                firstDay = Math.abs( new Date( year, month - 1, 1 ).getDay() ),

                dateToNum = util.dateToNum,
                joinDate = util.joinDate,

                tmpEptArr = [];
                tmpDayDataArr = [],
                tmpDayTplArr = [];

            //如果是星期天
            if ( firstDay == 0 ) {
                firstDay = 7;
            }

            //插入空白字符
            for ( var i = 0; i < firstDay - 1; i++ ) {
                tmpEptArr.push( '<li class="ept"></li>' );
            }

            for ( var i = 0; i < days; i++ ) {
                var day = i + 1,
                    date = joinDate( year, month, day ),
                    wkDay = new Date( date ).getDay(),
                    dateNum = dateToNum( date ),
                    jrClassName = 'dl jr';

                //默认不做任何处理
                tmpDayDataArr.push( {
                    class: '',
                    date: date,
                    day: day,
                    name: ''
                } );

                //双休单独标注出
                if ( wkDay == 6 || wkDay == 0 ) {
                    jrClassName = 'dl jr wk';
                    tmpDayDataArr[ i ][ 'class' ] = 'wk';
                }

                //不在指定范围内的日期置灰
                if ( ( me.minDate && dateNum < dateToNum( me.minDate ) ) ||
                    ( me.maxDate && dateNum > dateToNum( me.maxDate ) )
                ) {
                    jrClassName = 'dl iv';
                    tmpDayDataArr[ i ][ 'class' ] = 'iv';
                }

                //节假日处理
                if ( config.isShowHoliday ) {
                    for ( var k = 0, hlen = holidaysMap.length; k < hlen; k++ ) {
                        var name = holidaysMap[ k ][ 'name' ],
                            dateArr = holidaysMap[ k ][ 'date' ];

                        for ( var j = 0, len = dateArr.length; j < len; j++ ) {
                            var item = dateArr[ j ];

                            if ( dateNum == dateToNum( item ) ) {
                                tmpDayDataArr[ i ][ 'class' ] = jrClassName;
                                tmpDayDataArr[ i ][ 'name' ] = name;
                                break;
                            }
                        }
                    }
                }

                //初始化当前选中日期状态
                if ( config.selectDate ) {
                    if ( dateNum == dateToNum( me.selectDate ) ) {
                        tmpDayDataArr[ i ][ 'class' ] += ' cur';
                    }
                }
            }

            //生成日期模板字符串
            for ( var i = 0, len = tmpDayDataArr.length; i < len; i++ ) {
                var item = tmpDayDataArr[ i ];
                tmpDayTplArr.push(
                    '<li class="' + item.class + '" data-date="' + item.date + '">' +
                        '<i>' + item.day + '</i><i>' + item.name + '</i>' + 
                    '</li>'
                );
            }

            return tmpEptArr.concat( tmpDayTplArr ).join( '' );
        },

        /**
         * 设置选中日期格式
         * @para {date object|date string} YYYY-MM-DD
         */
        setSelectDate: function( date ) {
            var me = this,
                config = this.config,
                date = ( typeof date == 'string' ) ? date : util.formatDate( date ),
                dateNum = util.dateToNum( date ),

                lastSltItems = this.el.querySelectorAll( 'li.cur' ),
                curSltItem = this.el.querySelector( 'li[data-date="' + date + '"]' );
			
            //先移到上次选中日期高亮
            if (lastSltItems.length > 0) {
            	if (this.selectStep == 0) {
            		this._removeCur(lastSltItems[0]);
            		this._removeCur(lastSltItems[1]);
            	}
            }

            //添加当前选中日期高亮
            if ( curSltItem ) {
                var curDateNameEl = curSltItem.querySelectorAll( 'i' )[ 1 ];

                curSltItem.classList.add( 'cur' );
                var nameIndex = this.selectKey == 'secondDate' ? 1 : 0;
                if ( !curSltItem.classList.contains( 'jr' ) && this.selectNames[nameIndex] != '') {
                    curSltItem.classList.add( 'dl' );
                    curDateNameEl.innerText = this.selectNames[nameIndex];
                }
            }
            
            this._setSecondColor();
        },
		
		_removeCur: function(item) {
			if (!item) {
				return ;
			}
			var itemEl = item.querySelectorAll( 'i' )[ 1 ];
            item.classList.remove( 'cur' );
            if ( !item.classList.contains( 'jr' ) ) {
                item.classList.remove( 'dl' );
                itemEl.innerText = '';
            }
		},
		
		/**
		 * 设置从起始日期到结束日期之间的选中项
		 */
		_setSecondColor: function() {
			var lastSltItems = this.el.querySelectorAll( 'li.cur' ), oldLoads = this.el.querySelectorAll( 'li.load' ), curSltItem = null, curDateNameEl = null;
			for (var i = 0, j = oldLoads.length; i<j; i++) {
				oldLoads[i].classList.remove('load');
			}
			if (lastSltItems.length != 2) {
				return;
			}
			for (var i = util.dateToNum(this.selectDate), j = util.dateToNum(this.secondDate); i < j; i++) {
				curSltItem = this.el.querySelector( 'li[data-date="' + util.numToDate(i) + '"]' );
				if ( curSltItem ) {
                	curSltItem.classList.add('load');
               	}
			}
		},
		
        nextMonth: function() {
            var step = this.step;
            this.date = new Date( this.year, this.month + step - 1, 1 );
            this.render();
            this.month += step;
        },

        prevMonth: function() {
            var step = this.step;
            this.date = new Date( this.year, this.month - step - 1, 1 );
            this.render();
            this.month -= step;
        },

        show: function() {
            this.el.show();
            $.trigger( this, 'show' );
        },

        hide: function() {
            this.el.hide();
            $.trigger( this, 'hide' );
        },
        
        getDate: function(){
        	return util.formatDate(this.date);
        },
        
        getSelectDate: function(){
        	return util.formatDate(this.selectDate);
        }
    } );

    return {
        calendar: calendar,
        util: util
    };
} );
