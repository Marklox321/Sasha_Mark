"""
Веб-приложение для демонстрации численных методов решения уравнений
"""
from flask import Flask, render_template, request, jsonify
import math
import numpy as np

app = Flask(__name__)

class NumericalMethods:
    """
    Класс, реализующий основные численные методы решения уравнений
    """
    
    @staticmethod
    def bisection_method(f, a, b, eps=1e-6, max_iter=100):
        """
        Метод бисекции для нахождения корня уравнения f(x) = 0 на интервале [a, b]
        
        :param f: функция уравнения
        :param a: левая граница интервала
        :param b: правая граница интервала
        :param eps: точность
        :param max_iter: максимальное число итераций
        :return: корень уравнения или None
        """
        if f(a) * f(b) >= 0:
            return None
            
        for _ in range(max_iter):
            c = (a + b) / 2
            if abs(f(c)) < eps:
                return c
            if f(a) * f(c) < 0:
                b = c
            else:
                a = c
        return (a + b) / 2
    
    @staticmethod
    def newton_method(f, df, x0, eps=1e-6, max_iter=100):
        """
        Метод Ньютона для нахождения корня уравнения
        
        :param f: функция уравнения
        :param df: производная функции
        :param x0: начальное приближение
        :param eps: точность
        :param max_iter: максимальное число итераций
        :return: корень уравнения или None
        """
        x = x0
        for _ in range(max_iter):
            fx = f(x)
            if abs(fx) < eps:
                return x
            dfx = df(x)
            if dfx == 0:
                return None
            x = x - fx / dfx
        return x
    
    @staticmethod
    def simple_iteration_method(f, phi, x0, eps=1e-6, max_iter=100):
        """
        Метод простой итерации для решения уравнений
        
        :param f: функция уравнения
        :param phi: функция итерационного процесса
        :param x0: начальное приближение
        :param eps: точность
        :param max_iter: максимальное число итераций
        :return: корень уравнения или None
        """
        x = x0
        for _ in range(max_iter):
            x_new = phi(x)
            if abs(x_new - x) < eps and abs(f(x_new)) < eps:
                return x_new
            x = x_new
        return x

# Примеры функций для демонстрации
def example_f(x):
    """Пример функции: x^3 - 2x - 5 = 0"""
    return x**3 - 2*x - 5

def example_df(x):
    """Производная примерной функции: 3x^2 - 2"""
    return 3*x**2 - 2

def example_phi(x):
    """Итерационная функция для метода простых итераций"""
    return (2*x + 5)**(1/3)

@app.route('/')
def index():
    """Главная страница с формой ввода"""
    return render_template('index.html')

@app.route('/solve', methods=['POST'])
def solve():
    """API endpoint для решения уравнения"""
    data = request.json
    method = data.get('method')
    f_str = data.get('function')
    a = float(data.get('a', 0))
    b = float(data.get('b', 0))
    x0 = float(data.get('x0', 0))
    eps = float(data.get('eps', 1e-6))
    
    try:
        # Создаем функцию из строки (для демо используем примерную)
        f = example_f
        df = example_df
        phi = example_phi
        
        result = None
        if method == 'bisection':
            result = NumericalMethods.bisection_method(f, a, b, eps)
        elif method == 'newton':
            result = NumericalMethods.newton_method(f, df, x0, eps)
        elif method == 'iteration':
            result = NumericalMethods.simple_iteration_method(f, phi, x0, eps)
            
        return jsonify({
            'success': True,
            'result': result,
            'message': f'Решение найдено методом {method}'
        })
    except Exception as e:
        return jsonify({
            'success': False,
            'message': str(e)
        })

if __name__ == '__main__':
    app.run(debug=True)