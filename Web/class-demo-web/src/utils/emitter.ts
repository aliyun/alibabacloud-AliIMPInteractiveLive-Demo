import { BasicMap } from './index';
export default class Emitter {
  private handlerList: BasicMap<any[]> = {};
  private static instance: Emitter;
  static getInstance() {
    if (!this.instance) {
      this.instance = new Emitter();
    }
    return this.instance;
  }

  public emit(eventName: string, eventData?: any) {
    const eventHandlers = this.handlerList[eventName];
    if (!eventHandlers) return;
    for (let i = 0; i < eventHandlers.length; i++) {
      eventHandlers[i](eventData);
    }
  }

  public on(eventName: string, eventHandler: any) {
    if (!this.handlerList?.hasOwnProperty(eventName)) {
      this.handlerList[eventName] = [];
    }
    this.handlerList[eventName].push(eventHandler);
  }

  public remove(eventName: string, eventHandler?: any) {
    if (!eventHandler) {
      delete this.handlerList[eventName];
      return;
    }
    const eventHandlers = this.handlerList[eventName];
    const handlerIndex = eventHandlers.findIndex((e) => e === eventHandler);
    if (handlerIndex > -1) {
      this.handlerList[eventName].splice(handlerIndex, 1);
      if (this.handlerList[eventName].length === 0)
        delete this.handlerList[eventName];
    }
  }
}
